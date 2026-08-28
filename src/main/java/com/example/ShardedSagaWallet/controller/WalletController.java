package com.example.ShardedSagaWallet.controller;

import com.example.ShardedSagaWallet.Service.WalletService;
import com.example.ShardedSagaWallet.dtos.CreateWalletRequestDTO;
import com.example.ShardedSagaWallet.dtos.CreditWalletRequestDTO;
import com.example.ShardedSagaWallet.dtos.DebitWalletRequestDTO;
import com.example.ShardedSagaWallet.entities.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallets")
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody CreateWalletRequestDTO request) {
        Wallet wallet = walletService.createWallet(request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id){
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(wallet);
    }
    @GetMapping("/{id}/balance")
    public ResponseEntity<Double> getWalletBalance(@PathVariable Long id){
        BigDecimal balance = walletService.getWalletBalance(id);
        return ResponseEntity.ok(balance.doubleValue());
    }
    @PostMapping("/{userId}/debit")
    public ResponseEntity<Wallet> debitWallet(@PathVariable Long userId, @RequestBody DebitWalletRequestDTO request){
       Wallet wallet = walletService.debit(userId, request.getAmount());
       return ResponseEntity.ok(wallet);
    }
    @PostMapping("/{userId}/credit")
    public ResponseEntity<Wallet> creditWallet(@PathVariable Long userId, @RequestBody CreditWalletRequestDTO request) {
        Wallet wallet = walletService.credit(userId, request.getAmount());
        return ResponseEntity.ok(wallet);
    }
}
