package com.ecosort.backend.config;

import com.ecosort.backend.entity.*;
import com.ecosort.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WasteCategoryRepository categoryRepository;

    @Autowired
    private RewardItemRepository rewardItemRepository;

    @Autowired
    private CashBookRepository cashBookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedCommodities();
        seedRewardItems();
        seedInitialCashBook();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            // Petugas Account
            User officer = User.builder()
                    .fullName("Budi Santoso (Petugas)")
                    .phoneNumber("081234567890")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.PETUGAS)
                    .address("Kantor Bank Sampah RW 05, Kelurahan Asri")
                    .balance(BigDecimal.ZERO)
                    .createdAt(LocalDateTime.now().minusDays(30))
                    .build();
            userRepository.save(officer);

            // Warga Account 1
            User citizen1 = User.builder()
                    .fullName("Siti Aminah (Warga)")
                    .phoneNumber("081987654321")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.WARGA)
                    .address("Jl. Melati No. 12, RT 02/05")
                    .balance(new BigDecimal("25000.00"))
                    .createdAt(LocalDateTime.now().minusDays(15))
                    .build();
            userRepository.save(citizen1);

            // Warga Account 2
            User citizen2 = User.builder()
                    .fullName("Ahmad Pratama")
                    .phoneNumber("081112223333")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.WARGA)
                    .address("Jl. Mawar No. 04, RT 01/05")
                    .balance(new BigDecimal("85000.00"))
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build();
            userRepository.save(citizen2);
        }
    }

    private void seedCommodities() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(WasteCategory.builder()
                    .name("Botol Plastik PET Bening")
                    .description("Botol air mineral transparan tanpa warna. Nilai daur ulang tinggi untuk serat tekstil dan botol daur ulang.")
                    .unit("kg")
                    .pricePerKg(new BigDecimal("3500.00"))
                    .cleaningInstructions("Lepaskan label kemasan dan tutup botol. Cuci bersih dari sisa cairan, keringkan, lalu remas botol hingga pipih untuk menghemat ruang.")
                    .iconUrl("ic_pet_bottle")
                    .active(true)
                    .build());

            categoryRepository.save(WasteCategory.builder()
                    .name("Kardus Lembaran Bersih")
                    .description("Kardus gelombang cokelat tebal (corrugated box) bekas kemasan elektronik, logistik, atau makanan kering.")
                    .unit("kg")
                    .pricePerKg(new BigDecimal("2200.00"))
                    .cleaningInstructions("Bongkar lipatan kardus hingga lembaran mendatar. Bersihkan dari sisa lakban atau selotip plastik, pastikan kering dan ikat rapi.")
                    .iconUrl("ic_cardboard")
                    .active(true)
                    .build());

            categoryRepository.save(WasteCategory.builder()
                    .name("Kaleng Aluminium Minuman")
                    .description("Kaleng minuman ringan berbahan aluminium murni. Material dengan efisiensi lebur ulang 95%.")
                    .unit("kg")
                    .pricePerKg(new BigDecimal("14000.00"))
                    .cleaningInstructions("Bilas bagian dalam dengan air agar tidak mengundang semut, tiriskan hingga kering, lalu pipihkan untuk efisiensi timbangan.")
                    .iconUrl("ic_can")
                    .active(true)
                    .build());

            categoryRepository.save(WasteCategory.builder()
                    .name("Botol Kaca Utuh / Pecahan Bening")
                    .description("Botol kecap, sirup, atau selai kaca bening dalam kondisi utuh atau pecahan bersih tanpa kontaminasi keramik.")
                    .unit("kg")
                    .pricePerKg(new BigDecimal("1200.00"))
                    .cleaningInstructions("Bilas sisa cairan atau minyak. Lepaskan tutup logam atau plastik. Hindari mencampur pecahan dengan keramik atau porselen.")
                    .iconUrl("ic_glass")
                    .active(true)
                    .build());

            categoryRepository.save(WasteCategory.builder()
                    .name("Kertas Arsip / HVS Putih")
                    .description("Kertas dokumen perkantoran, lembar fotokopi, catatan sekolah, atau kertas binder putih polos.")
                    .unit("kg")
                    .pricePerKg(new BigDecimal("2800.00"))
                    .cleaningInstructions("Pastikan kertas dalam keadaan kering bebas minyak. Lepaskan staples, penjepit kertas besi (paper clip), dan sampul plastik mika.")
                    .iconUrl("ic_paper")
                    .active(true)
                    .build());
        }
    }

    private void seedRewardItems() {
        if (rewardItemRepository.count() == 0) {
            rewardItemRepository.save(RewardItem.builder()
                    .name("Paket Beras Premium 5 kg")
                    .description("Beras putih kualitas premium jenis Pandan Wangi/Rojolele pulen dan higienis langsung dari petani lokal.")
                    .requiredBalance(new BigDecimal("75000.00"))
                    .stockQuantity(25)
                    .imageUrl("ic_rice")
                    .active(true)
                    .build());

            rewardItemRepository.save(RewardItem.builder()
                    .name("Minyak Goreng Sawit 2 Liter")
                    .description("Minyak goreng kemasan bantal/pouch 2 liter jernih difortifikasi vitamin A untuk kebutuhan dapur harian.")
                    .requiredBalance(new BigDecimal("35000.00"))
                    .stockQuantity(40)
                    .imageUrl("ic_oil")
                    .active(true)
                    .build());

            rewardItemRepository.save(RewardItem.builder()
                    .name("Gula Pasir Kristal Putih 1 kg")
                    .description("Gula tebu murni kemasan 1 kg SNI untuk kebutuhan rumah tangga dan minuman hangat.")
                    .requiredBalance(new BigDecimal("17500.00"))
                    .stockQuantity(30)
                    .imageUrl("ic_sugar")
                    .active(true)
                    .build());

            rewardItemRepository.save(RewardItem.builder()
                    .name("Voucher Pulsa / Listrik Rp25.000")
                    .description("Kode voucher digital senilai Rp25.000 untuk pengisian pulsa all operator atau token listrik PLN.")
                    .requiredBalance(new BigDecimal("25000.00"))
                    .stockQuantity(50)
                    .imageUrl("ic_voucher")
                    .active(true)
                    .build());

            rewardItemRepository.save(RewardItem.builder()
                    .name("Penarikan Tunai Rp50.000")
                    .description("Pencairan saldo tabungan menjadi uang tunai pecahan rupiah di pos kas bank sampah komunitas.")
                    .requiredBalance(new BigDecimal("50000.00"))
                    .stockQuantity(100)
                    .imageUrl("ic_cash")
                    .active(true)
                    .build());
        }
    }

    private void seedInitialCashBook() {
        if (cashBookRepository.count() == 0) {
            CashBookEntry capital = CashBookEntry.builder()
                    .transactionType(CashBookType.CASH_INJECTION)
                    .amount(new BigDecimal("2000000.00"))
                    .balanceAfter(new BigDecimal("2000000.00"))
                    .referenceId(null)
                    .description("Modal awal kas operasional Bank Sampah Komunitas RW 05")
                    .build();
            cashBookRepository.save(capital);
        }
    }
}
