package com.example.ShardedSagaWallet.Service.saga.steps;

import com.example.ShardedSagaWallet.Service.saga.SagaContext;
import com.example.ShardedSagaWallet.Service.saga.SagaStep;
import com.example.ShardedSagaWallet.entities.Wallet;
import com.example.ShardedSagaWallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditDestinationWalletStep implements SagaStep {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {

        //Step 1: Get the destination wallet ID from the context
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");
        log.info("Executing CreditDestinationWalletStep for walletId: {} with amount: {}", toWalletId, amount);

        //Step 2: Fetch the destination wallet from the database with a lock to prevent concurrent modifications
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found with id: " + toWalletId));
        log.info("Fetched destination wallet with id: {} and current balance: {}", wallet.getId(), wallet.getBalance());
        context.put("originalToWalletBalance", wallet.getBalance());

        //Step 3: Credit the specified amount to the destination wallet

        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with new balance: {}", wallet.getBalance());
        context.put("toWalletBalanceAfterCredit", wallet.getBalance());

        log.info("CreditDestinationWalletStep executed successfully for walletId: {} with amount: {}", toWalletId, amount);
        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");
        log.info("Compensating Credit of Destination Wallet for walletId: {} with amount: {}", toWalletId, amount);
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found with id: " + toWalletId));
        log.info("Wallet fetched with balance: {}", wallet.getBalance());
        wallet.debit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with balance: {}", wallet.getBalance());
        context.put("toWalletBalanceAfterCreditCompensation", wallet.getBalance());
        log.info("Credit compensation of destination wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return "CreditDestinationWalletStep";
    }
}
