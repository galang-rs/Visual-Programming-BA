package com.ecosort.backend;

import com.ecosort.backend.dto.CreateDepositRequest;
import com.ecosort.backend.dto.DepositDto;
import com.ecosort.backend.entity.*;
import com.ecosort.backend.exception.BadRequestException;
import com.ecosort.backend.repository.*;
import com.ecosort.backend.service.DepositService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DepositServiceTest {

    @Autowired
    private DepositService depositService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WasteCategoryRepository categoryRepository;

    @Autowired
    private WasteDepositRepository depositRepository;

    private User officer;
    private User citizen;
    private WasteCategory petCategory;

    @BeforeEach
    void setUp() {
        officer = userRepository.findByPhoneNumber("081234567890").orElseThrow();
        citizen = userRepository.findByPhoneNumber("081987654321").orElseThrow();
        petCategory = categoryRepository.findAll().stream()
                .filter(c -> c.getName().contains("PET"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Uji Coba 3: Penyetoran Fisik dan Penimbangan di Bank Sampah (Happy Path)")
    void testCreateDeposit_Success() {
        BigDecimal initialBalance = citizen.getBalance();
        BigDecimal weight = new BigDecimal("5.00");
        BigDecimal expectedPayout = petCategory.getPricePerKg().multiply(weight);

        CreateDepositRequest request = CreateDepositRequest.builder()
                .citizenId(citizen.getId())
                .categoryId(petCategory.getId())
                .weightKg(weight)
                .notes("Setoran botol bersih")
                .build();

        DepositDto result = depositService.createDeposit(officer, request);

        assertNotNull(result);
        assertEquals(0, expectedPayout.compareTo(result.getTotalAmount()));
        assertEquals(0, initialBalance.add(expectedPayout).compareTo(result.getCitizenBalanceAfter()));

        // Verify citizen refreshed balance in database
        User updatedCitizen = userRepository.findById(citizen.getId()).orElseThrow();
        assertEquals(0, initialBalance.add(expectedPayout).compareTo(updatedCitizen.getBalance()));
    }

    @Test
    @DisplayName("Kasus 1: Masukan Bobot Timbangan Tidak Valid atau Bernilai Negatif (Edge Case)")
    void testCreateDeposit_NegativeWeight_ThrowsBadRequest() {
        CreateDepositRequest request = CreateDepositRequest.builder()
                .citizenId(citizen.getId())
                .categoryId(petCategory.getId())
                .weightKg(new BigDecimal("-1.50"))
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            depositService.createDeposit(officer, request);
        });

        assertTrue(ex.getMessage().contains("lebih besar dari 0"));
    }

    @Test
    @DisplayName("Kasus 1: Masukan Bobot Timbangan Bernilai Nol (Edge Case)")
    void testCreateDeposit_ZeroWeight_ThrowsBadRequest() {
        CreateDepositRequest request = CreateDepositRequest.builder()
                .citizenId(citizen.getId())
                .categoryId(petCategory.getId())
                .weightKg(BigDecimal.ZERO)
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            depositService.createDeposit(officer, request);
        });

        assertTrue(ex.getMessage().contains("lebih besar dari 0"));
    }
}
