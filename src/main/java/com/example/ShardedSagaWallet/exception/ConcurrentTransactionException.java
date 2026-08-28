package com.example.ShardedSagaWallet.exception;

public class ConcurrentTransactionException extends RuntimeException {
    public ConcurrentTransactionException(String lockKey) {
        super("Wallet/Resource is currently processing another transaction (" + lockKey + "). Please try again.");
    }

    public ConcurrentTransactionException(String lockKey, Throwable cause) {
        super("Lock acquisition interrupted for key: " + lockKey, cause);
    }
}