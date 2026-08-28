package com.example.ShardedSagaWallet.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("walletId")
    private Long id;

    @Column(name = "user_id", nullable = false ,updatable = false)
    private Long userId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    public boolean hassufficientBalance(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    public void debit(BigDecimal amount) {
        if (!hassufficientBalance(amount)) {
            throw new IllegalArgumentException("Insufficient balance in wallet with id: " + id);
        }
        balance = balance.subtract(amount);
    }
    public void credit(BigDecimal amount) {
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        balance = balance.add(amount);
    }
}
