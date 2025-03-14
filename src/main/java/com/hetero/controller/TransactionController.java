package com.hetero.controller;


import com.hetero.models.Platform;
import com.hetero.models.Transaction;
import com.hetero.service.LedgerService;
import com.hetero.service.LedgerServiceImpl;
import com.hetero.service.TransactionService;

import com.hetero.utils.ApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = {"http://52.66.253.103", "http://localhost:8008"})
public class TransactionController {
    private static final Logger log = LogManager.getLogger(TransactionController.class);
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerService ledgerService;

    @PostMapping("/add")
    public ResponseEntity<?> addTransaction(@RequestBody Transaction transaction) {
        try{
            Transaction savedTransaction = transactionService.addTransaction(transaction);
            ApiResponse<Transaction> apiResponse = new ApiResponse<>(200, "Transaction added", savedTransaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        }catch (ConcurrentModificationException e) {
            ApiResponse<Transaction> apiResponse = new ApiResponse<>(409, "Transaction already exists", null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Transaction failed", null));
        }
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();

            if (transactions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(new ApiResponse<>(
                                HttpStatus.NO_CONTENT.value(),
                                "No transactions found",
                                transactions));
            }

            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Transactions retrieved successfully", transactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve transactions", null));
        }
    }



    @PostMapping("/platform/all")
    public ResponseEntity<?> getAllTransactionsByPlatform(@RequestParam String platform) {
        try {
            // Convert String to Enum (Case-insensitive validation)
            Platform platformEnum = Arrays.stream(Platform.values())
                    .filter(p -> p.name().equalsIgnoreCase(platform))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid platform type. Allowed values: iPeyU, Secondary, ALL"));

            // Fetch transactions by platform type
            List<Transaction> transactions = transactionService.getAllTransactionsByPlatformType(platformEnum);

            // Return 200 OK with transactions
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Transactions retrieved successfully", transactions));
        } catch (IllegalArgumentException e) {
            // Return 400 Bad Request if the platform type is invalid
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid platform type. Allowed values: iPeyU, Secondary, ALL", null));
        } catch (Exception e) {
            // Return 500 Internal Server Error in case of unexpected issues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve transactions", null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> getTransactionById(@RequestParam Long id) {
        try {
            Transaction transaction = transactionService.getTransactionById(id);

            if (transaction == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Transaction not found", null));
            }

            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Transaction retrieved successfully", transaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error retrieving transaction", null));
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTransactionById(@PathVariable Long id) {
        ApiResponse<String> response = transactionService.deleteTransactionById(id);

        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Transaction>> updateTransaction(
            @PathVariable Long id,
            @RequestBody Transaction transaction) {

        Transaction existingTransaction = transactionService.getTransactionById(id);

        if (existingTransaction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Transaction not found", null));
        }

        try {
            ledgerService.updateLedgerWithRetry(new LedgerServiceImpl.TransactionUpdate(
                    LedgerServiceImpl.UpdateType.TRANSACTION,
                    transaction,
                    false,
                    existingTransaction,
                    null
            ));

            ApiResponse<Transaction> response = transactionService.updateTransaction(id, transaction);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (ConcurrentModificationException e) {
            log.error("Failed to update ledger for transaction update: {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(409, "Failed to update ledger", null));
        }
    }

}