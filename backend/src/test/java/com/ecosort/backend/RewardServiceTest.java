package com.ecosort.backend;

import com.ecosort.backend.dto.ClaimRewardRequest;
import com.ecosort.backend.dto.RewardClaimDto;
import com.ecosort.backend.dto.VerifyClaimRequest;
import com.ecosort.backend.entity.ClaimStatus;
import com.ecosort.backend.entity.RewardItem;
import com.ecosort.backend.entity.User;
import com.ecosort.backend.exception.BadRequestException;
import com.ecosort.backend.repository.RewardItemRepository;
import com.ecosort.backend.repository.UserRepository;
import com.ecosort.backend.service.RewardService;
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
class RewardServiceTest {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RewardItemRepository rewardItemRepository;

    private User officer;
    private User citizen;

    @BeforeEach
    void setUp() {
        officer = userRepository.findByPhoneNumber("081234567890").orElseThrow();
        citizen = userRepository.findByPhoneNumber("081987654321").orElseThrow();
    }

    @Test
    @DisplayName("Kasus 3: Saldo Tabungan Tidak Mencukupi Saat Pengajuan Reward (Edge Case)")
    void testClaimReward_InsufficientBalance_ThrowsBadRequest() {
        // Find high price reward like Beras 5kg (Rp75.000) while citizen has Rp25.000
        RewardItem expensiveItem = rewardItemRepository.findAll().stream()
                .filter(item -> item.getRequiredBalance().compareTo(citizen.getBalance()) > 0)
                .findFirst()
                .orElseThrow();

        ClaimRewardRequest request = ClaimRewardRequest.builder()
                .rewardItemId(expensiveItem.getId())
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            rewardService.claimReward(citizen, request);
        });

        assertTrue(ex.getMessage().contains("Saldo tabungan tidak mencukupi"));
    }

    @Test
    @DisplayName("Uji Coba 4: Penukaran Saldo Tabungan dengan Reward & Verifikasi Petugas (Happy Path)")
    void testClaimReward_Success_And_Verify() {
        // Find affordable item like Gula Pasir 1kg (Rp17.500)
        RewardItem affordableItem = rewardItemRepository.findAll().stream()
                .filter(item -> item.getRequiredBalance().compareTo(citizen.getBalance()) <= 0)
                .findFirst()
                .orElseThrow();

        BigDecimal balanceBefore = citizen.getBalance();
        ClaimRewardRequest request = ClaimRewardRequest.builder()
                .rewardItemId(affordableItem.getId())
                .build();

        RewardClaimDto claim = rewardService.claimReward(citizen, request);

        assertNotNull(claim);
        assertNotNull(claim.getClaimVoucherCode());
        assertEquals(ClaimStatus.PENDING, claim.getStatus());

        // Verify balance debited
        User updatedCitizen = userRepository.findById(citizen.getId()).orElseThrow();
        assertEquals(0, balanceBefore.subtract(affordableItem.getRequiredBalance()).compareTo(updatedCitizen.getBalance()));

        // Officer verifies claim
        VerifyClaimRequest verifyRequest = VerifyClaimRequest.builder()
                .claimVoucherCode(claim.getClaimVoucherCode())
                .build();

        RewardClaimDto completedClaim = rewardService.verifyAndCompleteClaim(officer, verifyRequest);
        assertEquals(ClaimStatus.COMPLETED, completedClaim.getStatus());
        assertEquals(officer.getId(), completedClaim.getOfficerId());
    }
}
