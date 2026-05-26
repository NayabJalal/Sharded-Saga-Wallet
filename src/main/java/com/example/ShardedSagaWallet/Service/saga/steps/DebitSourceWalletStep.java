package com.example.ShardedSagaWallet.Service.saga.steps;

import com.example.ShardedSagaWallet.Service.saga.SagaContext;
import com.example.ShardedSagaWallet.Service.saga.SagaStepInterface;
import com.example.ShardedSagaWallet.entities.Wallet;
import com.example.ShardedSagaWallet.Service.saga.steps.SagaStepFactory.SagaStepType;
import com.example.ShardedSagaWallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebitSourceWalletStep implements SagaStepInterface {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Debiting source wallet with id: {} for amount: {}", fromWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Source wallet not found with id: " + fromWalletId));

        log.info("Wallet fetched with balance: {}", wallet.getBalance());
        context.put("originalSourceWalletBalance", wallet.getBalance());

        walletRepository.updateBalanceByUserId(fromWalletId, wallet.getBalance().subtract(amount));

        log.info("Wallet saved with new balance: {}", wallet.getBalance());
        context.put("sourceWalletBalanceAfterDebit", wallet.getBalance());
        log.info("Debit source wallet step executed successfully");

        return true;
    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensating debit of source wallet with id: {} for amount: {}", fromWalletId, amount);
        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Source wallet not found with id: " + fromWalletId));

        log.info("Source wallet fetched with balance: {}", wallet.getBalance());
        context.put("sourceWalletBalanceBeforeCreditCompensation", wallet.getBalance());

        walletRepository.updateBalanceByUserId(fromWalletId, wallet.getBalance().add(amount));

        log.info("Source wallet saved with balance: {}", wallet.getBalance());
        context.put("sourceWalletBalanceAfterCreditCompensation", wallet.getBalance());
        log.info("Compensating source wallet step executed successfully");

        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString();
    }
}
