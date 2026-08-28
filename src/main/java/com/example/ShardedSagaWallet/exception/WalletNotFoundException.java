package com.example.ShardedSagaWallet.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(Long walletId) {
        super("Wallet not found with ID: " + walletId);
    }
}