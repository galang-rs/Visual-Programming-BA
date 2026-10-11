package com.ecosort.backend.service;

import com.ecosort.backend.dto.UserProfileResponse;
import com.ecosort.backend.entity.Role;
import com.ecosort.backend.entity.User;
import com.ecosort.backend.repository.UserRepository;
import com.ecosort.backend.repository.WasteDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Layer: UserService
 * 
 * Mengelola data profil pengguna, pencarian data nasabah warga oleh Petugas Bank Sampah,
 * serta kalkulasi agregasi statistik individu seperti akumulasi total berat sampah yang disetor.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WasteDepositRepository depositRepository;

    /**
     * Mencari nasabah (Role: WARGA) berdasarkan nama lengkap atau nomor telepon.
     * Digunakan oleh Petugas pada formulir pos penimbangan untuk menemukan akun nasabah.
     * 
     * @param query Kata kunci pencarian (nama atau no. HP). Jika null/kosong, mengembalikan seluruh warga.
     * @return List UserProfileResponse profil nasabah lengkap dengan saldo dan total berat sampah
     */
    public List<UserProfileResponse> searchCitizens(String query) {
        List<User> citizens;
        // [LOGIKA 1] Percabangan Pencarian:
        // Jika ada query -> cari case-insensitive pada nama atau nomor telepon.
        // Jika query kosong -> tampilkan seluruh daftar warga terdaftar.
        if (query != null && !query.trim().isEmpty()) {
            citizens = userRepository.searchByRoleAndQuery(Role.WARGA, query.trim());
        } else {
            citizens = userRepository.findByRole(Role.WARGA);
        }

        return citizens.stream()
                .map(this::toProfileDto)
                .collect(Collectors.toList());
    }

    /**
     * Konversi entitas User ke UserProfileResponse dengan agregasi statistik berat sampah.
     * 
     * [LOGIKA 2] Menghitung akumulasi total berat sampah (kg) yang pernah disetor nasabah
     * melalui query agregasi SUM di repositori deposit.
     */
    public UserProfileResponse toProfileDto(User user) {
        BigDecimal totalWeight = depositRepository.getTotalWeightByCitizenId(user.getId());
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .address(user.getAddress())
                .balance(user.getBalance())
                .totalWeightDeposited(totalWeight != null ? totalWeight : BigDecimal.ZERO)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
