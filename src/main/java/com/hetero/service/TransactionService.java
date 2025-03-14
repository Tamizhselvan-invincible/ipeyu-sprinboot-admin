package com.hetero.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hetero.exception.TransactionNotUpdateException;
import com.hetero.models.Platform;
import com.hetero.models.Transaction;
import com.hetero.models.telecom.PaymentRequest;
import com.hetero.utils.ApiResponse;

import java.util.List;

public interface TransactionService {

    Transaction addTransaction(Transaction transaction);
    List<Transaction> getAllTransactions();
    Transaction getTransactionById(Long id);
    List<Transaction> getAllTransactionsByPlatformType(Platform platform);
    ApiResponse<String> deleteTransactionById(Long id);
    ApiResponse<Transaction> updateTransaction(Long id, Transaction transaction);
    Transaction updateTransactionByPaymentRequest(PaymentRequest paymentRequest, JsonNode jsonData) throws TransactionNotUpdateException;
}
