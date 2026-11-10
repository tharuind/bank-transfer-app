package com.example.bankapp.dto;

import com.example.bankapp.model.BankTransaction;

import java.math.BigDecimal;
import java.util.List;

public record TransferApiResponse(
        String message,
        Long transactionId,
        String status,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        List<String> errors) {

    public static TransferApiResponse success(String message, BankTransaction transaction) {
        return new TransferApiResponse(
                message,
                transaction.getId(),
                transaction.getStatus().name(),
                transaction.getFromAccount(),
                transaction.getToAccount(),
                transaction.getAmount(),
                List.of());
    }

    public static TransferApiResponse failure(String message, List<String> errors) {
        return new TransferApiResponse(message, null, null, null, null, null, errors);
    }
}
