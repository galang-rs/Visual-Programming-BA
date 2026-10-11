package com.ecosort.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reward_claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reward_item_id", nullable = false)
    private RewardItem rewardItem;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal requiredBalance;

    @Column(nullable = false, unique = true, length = 32)
    private String claimVoucherCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "officer_id")
    private User officer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime claimedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (claimedAt == null) {
            claimedAt = LocalDateTime.now();
        }
    }
}
