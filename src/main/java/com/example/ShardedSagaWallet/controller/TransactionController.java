package com.example.ShardedSagaWallet.controller;

import com.example.ShardedSagaWallet.Service.TransactionService;
import com.example.ShardedSagaWallet.Service.TransferSagaService;
import com.example.ShardedSagaWallet.dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final TransferSagaService transferSagaService;

    @PostMapping
    public ResponseEntity<TransferResponseDTO> createTransaction(@RequestBody TransferRequestDTO request){
        try{
            Long sagaInstanceId = transferSagaService.initiateTransfer(
                    request.getFromWalletId(),
                    request.getToWalletId(),
                    request.getAmount(),
                    request.getDescription()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    TransferResponseDTO.builder()
                            .sagaInstanceId(sagaInstanceId)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error initiating transfer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
