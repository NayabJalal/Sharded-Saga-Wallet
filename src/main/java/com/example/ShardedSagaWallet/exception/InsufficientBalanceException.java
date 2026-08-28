package com.example.ShardedSagaWallet.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(Long walletId, BigDecimal amount) {
        super("Wallet ID " + walletId + " has insufficient funds for transaction amount: " + amount);
    }
}