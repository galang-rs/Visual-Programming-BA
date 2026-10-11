package com.ecosort.backend.service;

import com.ecosort.backend.config.JwtTokenProvider;
import com.ecosort.backend.dto.AuthRequest;
import com.ecosort.backend.dto.AuthResponse;
import com.ecosort.backend.dto.UserProfileResponse;
import com.ecosort.backend.entity.Role;
import com.ecosort.backend.entity.User;
import com.ecosort.backend.exception.BadRequestException;
import com.ecosort.backend.exception.ResourceNotFoundException;
import com.ecosort.backend.exception.UnauthorizedException;
import com.ecosort.backend.repository.UserRepository;
import com.ecosort.backend.repository.WasteDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service Layer: AuthService
 * 
 * Mengelola alur pendaftaran akun pengguna baru (Warga/Petugas),
 * proses verifikasi kredensial login dengan enkripsi BCrypt,
 * penerbitan token sesi kriptografis JWT tanpa status (Stateless Session),
 * serta penyajian data profil pengguna aktif.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WasteDepositRepository depositRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Memproses pendaftaran akun pengguna baru.
     * 
     * Alur Logika Bisnis:
     * 1. Validasi keunikan nomor telepon (mencegah duplikasi akun).
     * 2. Penentuan peran default: Role.WARGA jika tidak ditentukan.
     * 3. Pengacakan satu arah kata sandi (Password Hashing) menggunakan BCrypt.
     * 4. Inisialisasi buku tabungan nasabah dengan saldo awal Rp0.
     * 5. Penyimpanan entitas User ke basis data relasional.
     * 6. Penerbitan token JWT sesi awal agar pengguna langsung login setelah mendaftar.
     * 
     * @param request DTO pendaftaran (fullName, phoneNumber, password, address, role)
     * @return AuthResponse memuat token JWT dan data ringkas profil
     */
    @Transactional
    public AuthResponse register(AuthRequest.Register request) {
        // [LOGIKA 1] Pemeriksaan Keunikan Nomor Telepon
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Nomor telepon sudah terdaftar dalam sistem");
        }

        // [LOGIKA 2] Penentuan Peran (Role-Based Access)
        Role role = request.getRole() != null ? request.getRole() : Role.WARGA;

        // [LOGIKA 3] Pembuatan Objek Pengguna & Enkripsi Kata Sandi (BCrypt)
        User user = User.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .role(role)
                .balance(BigDecimal.ZERO) // [LOGIKA 4] Saldo awal selalu Rp0
                .build();

        user = userRepository.save(user);

        // [LOGIKA 5] Penerbitan Token Akses Digital (JWT)
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPhoneNumber(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .balance(user.getBalance())
                .build();
    }

    /**
     * Memverifikasi kredensial login pengguna.
     * 
     * Alur Logika Bisnis:
     * 1. Pencarian akun berdasarkan nomor telepon.
     * 2. Verifikasi kecocokan kata sandi dengan hash BCrypt yang tersimpan.
     * 3. Jika cocok, sistem menerbitkan tiket akses digital (token JWT).
     * 
     * @param request DTO login (phoneNumber, password)
     * @return AuthResponse token JWT dan data sesi pengguna
     */
    public AuthResponse login(AuthRequest.Login request) {
        // [LOGIKA 1] Pencarian Akun Pengguna
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber().trim())
                .orElseThrow(() -> new UnauthorizedException("Nomor telepon atau kata sandi salah"));

        // [LOGIKA 2] Verifikasi Kata Sandi Menggunakan BCrypt Matcher
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Nomor telepon atau kata sandi salah");
        }

        // [LOGIKA 3] Pembuatan Token JWT Bertanda Tangan Kriptografis
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPhoneNumber(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .balance(user.getBalance())
                .build();
    }

    /**
     * Mengambil profil lengkap akun pengguna yang sedang terotentikasi.
     * 
     * @param currentUser Entitas user dari Spring Security Context
     * @return UserProfileResponse profil nasabah beserta total berat sampah yang disetor
     */
    public UserProfileResponse getProfile(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // [LOGIKA 1] Agregasi Statistik Berat Sampah Terkumpul Milik Pengguna
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
