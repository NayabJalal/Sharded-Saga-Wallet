package com.example.ShardedSagaWallet.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequestDTO {
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private String description;
}
