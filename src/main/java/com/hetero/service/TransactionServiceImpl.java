package com.hetero.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hetero.exception.TransactionNotUpdateException;
import com.hetero.models.*;
import com.hetero.models.telecom.PaymentRequest;
import com.hetero.repository.SubscriptionPlanDao;
import com.hetero.repository.TransactionDao;
import com.hetero.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    @Autowired
    TransactionDao transactionDao;

    @Autowired
    SubscriptionPlanDao subscriptionPlanDao;

    @Autowired
    UserService userService;

    @Autowired
    private LedgerService ledgerService;

    @Transactional
    @Override
    public Transaction addTransaction (Transaction transaction) {

        User user = userService.getUser(transaction.getUserId());
        List<Transaction> transactions = user.getTransactions();
        if (transactions.isEmpty()) {
            transactions = new ArrayList<>();
        }
        transactions.add(transaction);
        user.setTransactions(transactions);
        if (transaction.getSubscriptionPlan() != null) {
            SubscriptionPlan plan = subscriptionPlanDao.save(transaction.getSubscriptionPlan());
            transaction.setSubscriptionPlan(plan);
        }

        Transaction savedTransaction =  transactionDao.save(transaction);
        try {
            ledgerService.updateLedgerWithRetry(new LedgerServiceImpl.TransactionUpdate(
                    LedgerServiceImpl.UpdateType.TRANSACTION,
                    savedTransaction,
                    false,
                    null,
                    null
            ));
        } catch (ConcurrentModificationException e) {
            log.error("Failed to update ledger for transaction: {}", savedTransaction.getId(), e);
        }

        return savedTransaction;
    }

    @Transactional
    @Override
    public List<Transaction> getAllTransactions () {
        return transactionDao.findAll();
    }


    @Transactional
    @Override
    public List<Transaction> getAllTransactionsByPlatformType(Platform platform){
        return transactionDao.findByPlatformType(platform);
    }


    @Transactional
    @Override
    public Transaction getTransactionById (Long id) {
        return transactionDao.findById(id).get();
    }

    @Transactional
    @Override
    public ApiResponse<String> deleteTransactionById(Long id) {
        Optional<Transaction> optionalTransaction = transactionDao.findById(id);

        if (optionalTransaction.isEmpty()) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Transaction Not Found", null);
        }

        Transaction transaction = optionalTransaction.get();

        try {
            ledgerService.updateLedgerWithRetry(new LedgerServiceImpl.TransactionUpdate(
                    LedgerServiceImpl.UpdateType.TRANSACTION,
                    transaction,
                    true,
                    transaction,
                    null
            ));

            transaction.setDeleted(true);
            transaction.setDeletedAt(Instant.now().getEpochSecond());
            transactionDao.save(transaction); // Soft delete

            return new ApiResponse<>(HttpStatus.OK.value(), "Transaction deleted successfully", null);
        } catch (ConcurrentModificationException e) {
            log.error("Failed to update ledger for deletion of transaction: {}", id, e);
            return new ApiResponse<>(HttpStatus.CONFLICT.value(), "Failed to update ledger", null);
        }
    }


    @Transactional
    @Override
    public ApiResponse<Transaction> updateTransaction(Long id, Transaction transaction) {
        Optional<Transaction> optionalTransaction = transactionDao.findById(id);

        if (optionalTransaction.isEmpty()) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Transaction not found", null);
        }

        Transaction existingTransaction = optionalTransaction.get();
        existingTransaction.setAmount(transaction.getAmount());
        existingTransaction.setAggregatedTransactionId(transaction.getAggregatedTransactionId());
        existingTransaction.setTransactionReference(transaction.getTransactionReference());
        existingTransaction.setDeleted(transaction.isDeleted());
        existingTransaction.setUserId(transaction.getUserId());
        existingTransaction.setCashBack(transaction.getCashBack());
        existingTransaction.setStatus(transaction.getStatus());
        existingTransaction.setDeletedAt(transaction.getDeletedAt());

        Transaction updatedTransaction = transactionDao.save(existingTransaction);

        return new ApiResponse<>(HttpStatus.OK.value(), "Transaction updated successfully", updatedTransaction);
    }


    @Transactional
    @Override
    public Transaction updateTransactionByPaymentRequest (PaymentRequest paymentRequest, JsonNode jsonData) throws TransactionNotUpdateException {
        String status = jsonData.get("status").asText();
        String payId;
        payId = jsonData.get("payid").asText();
        if (!jsonData.has("payid")){
            payId = "-1";
        }
        if (payId == null || payId.isEmpty()) {
            payId = "-1";
        }

        SubscriptionPlan subscriptionPlan = paymentRequest.getSubscriptionPlan();


        Transaction transaction = new Transaction();
        transaction.setAggregatedTransactionId(payId);
        if (status.equals("success")) {
            transaction.setStatus(TransactionStatus.Success);
        } else if (status.equals("failure")) {
            transaction.setStatus(TransactionStatus.Failed);
        } else {
            transaction.setStatus(TransactionStatus.Processing);
        }
        transaction.setCashBack(String.valueOf(paymentRequest.getCashback()));
        transaction.setUserId(paymentRequest.getUserID());
        transaction.setSubscriptionPlan(subscriptionPlan);
        transaction.setAmount(String.valueOf(paymentRequest.getAmount()));
        transaction.setPlatformType(paymentRequest.getPlatform());
        transaction.setPaymentMethod(paymentRequest.getPaymentMethod());
        transaction.setTxnImage(paymentRequest.getTxnImage());
        transaction.setTxnUserName(paymentRequest.getTxnUserName());
        transaction.setTxnUserId(paymentRequest.getTxnUserId());
        transaction.setTxnStatus(paymentRequest.getTxnStatus());
        transaction.setTypeOfTransaction(paymentRequest.getTypeOfTransaction());

    userService.updateUserCashBackTransactions(
            paymentRequest.getUserID(),
            paymentRequest.getCashback()
    );

    return addTransaction(transaction);
    }




}
