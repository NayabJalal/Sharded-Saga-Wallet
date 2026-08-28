package com.example.ShardedSagaWallet.Service.saga.steps;

import com.example.ShardedSagaWallet.Service.saga.SagaContext;
import com.example.ShardedSagaWallet.Service.saga.SagaStepInterface;
import com.example.ShardedSagaWallet.Service.saga.steps.SagaStepFactory.SagaStepType;
import com.example.ShardedSagaWallet.entities.Wallet;
import com.example.ShardedSagaWallet.repository.WalletRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("CREDIT_DESTINATION_WALLET_STEP")
@RequiredArgsConstructor
@Slf4j
public class CreditDestinationWalletStep implements SagaStepInterface {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Executing CreditDestinationWalletStep for walletId: {} with amount: {}", toWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new IllegalArgumentException("Destination wallet not found with id: " + toWalletId));

        context.put("originalToWalletBalance", wallet.getBalance());

        wallet.credit(amount);
        walletRepository.save(wallet);

        log.info("Destination wallet saved with new balance: {}", wallet.getBalance());
        context.put("toWalletBalanceAfterCredit", wallet.getBalance());

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

        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("Destination wallet balance reverted to: {}", wallet.getBalance());
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    }
}