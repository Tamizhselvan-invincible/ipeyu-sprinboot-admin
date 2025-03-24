package com.hetero.service;

import com.hetero.models.Ledger;
import com.hetero.models.Transaction;
import com.hetero.models.TransactionStatus;
import com.hetero.repository.LedgerDao;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.Date;


@Slf4j
@Service
public class LedgerServiceImpl implements LedgerService {

    private static final int RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;

    @Autowired
    LedgerDao ledgerDao;



    @PostConstruct
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void init() {
       if (ledgerDao.count() == 0) {
           Ledger ledger = new Ledger();
           ledger.setVersion(1);
           ledger.setTotalAmount(new BigDecimal("0"));
           ledger.setTotalTransactions(0);
           ledger.setFailedTransactions(0);
           ledger.setNoOfUsers(0);
           ledger.setNoOfBlockedUsers(0);
           ledger.setStatsDate(Instant.now().getEpochSecond());
           ledgerDao.save(ledger);
       }

    }

    @Transactional
    public Ledger getLedger() {
//      return ledgerDao.findById(1).orElseGet(this::createInitialLedger);
        return ledgerDao.findFirstByOrderByIdAsc().orElseThrow(() -> new EntityNotFoundException("Ledger not found"));
    }



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public Ledger updateLedgerWithRetry(TransactionUpdate update) {
        int attempt = 0;
        while (attempt < RETRY_ATTEMPTS) {
            try {
                return doUpdateLedger(update);
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                if (attempt == RETRY_ATTEMPTS) {
                    throw new ConcurrentModificationException("Failed to update ledger after " + RETRY_ATTEMPTS + " attempts");
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry", ie);
                }
            }
        }
        throw new RuntimeException("Failed to update ledger");
    }



    @Transactional(propagation = Propagation.MANDATORY)
     Ledger doUpdateLedger(TransactionUpdate update) {
        Ledger ledger = ledgerDao.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new EntityNotFoundException("Ledger not found"));

        if (update.getType() == UpdateType.TRANSACTION) {
            updateTransactionStats(ledger, update.getTransaction(), update.isDelete(), update.getOldTransaction());
        } else {
            updateUserStats(ledger, update.getUserUpdate());
        }

        return ledgerDao.saveAndFlush(ledger);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateTransactionStats(Ledger ledger, Transaction transaction,
                                        boolean isDelete, Transaction oldTransaction) {
        if (isDelete) {
            handleTransactionDeletion(ledger, oldTransaction);
        } else if (oldTransaction != null) {
            handleTransactionUpdate(ledger, transaction, oldTransaction);
        } else {
            handleNewTransaction(ledger, transaction);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void handleTransactionDeletion(Ledger ledger, Transaction oldTransaction) {
        BigDecimal transactionAmount = new BigDecimal(String.valueOf(oldTransaction.getAmount()));
        ledger.setTotalAmount(ledger.getTotalAmount().subtract(transactionAmount));
        ledger.setTotalTransactions(ledger.getTotalTransactions() - 1);

        if (oldTransaction.getStatus() == TransactionStatus.Failed) {
            ledger.setTotalFailedAmount(ledger.getTotalFailedAmount()
                    .subtract(transactionAmount));
            ledger.setFailedTransactions(ledger.getFailedTransactions() - 1);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void handleTransactionUpdate(Ledger ledger, Transaction newTransaction,
                                         Transaction oldTransaction) {
        BigDecimal oldTransactionAmount = new BigDecimal(String.valueOf(oldTransaction.getAmount()));
        BigDecimal newTransactionAmount = new BigDecimal(String.valueOf(newTransaction.getAmount()));
        // Update total amount
        ledger.setTotalAmount(ledger.getTotalAmount()
                .subtract(oldTransactionAmount)
                .add(newTransactionAmount));

        // Handle failed transaction status changes
        if (oldTransaction.getStatus() == TransactionStatus.Failed) {
            ledger.setTotalFailedAmount(ledger.getTotalFailedAmount()
                    .subtract(oldTransactionAmount));
            ledger.setFailedTransactions(ledger.getFailedTransactions() - 1);
        }

        if (newTransaction.getStatus() == TransactionStatus.Failed) {
            ledger.setTotalFailedAmount(ledger.getTotalFailedAmount()
                    .add(newTransactionAmount));
            ledger.setFailedTransactions(ledger.getFailedTransactions() + 1);
        }
    }


@Transactional
protected void handleNewTransaction(Ledger ledger, Transaction transaction) {
    BigDecimal transactionAmount = new BigDecimal(String.valueOf(transaction.getAmount()));
        ledger.setTotalAmount(ledger.getTotalAmount().add(transactionAmount));
        ledger.setTotalTransactions(ledger.getTotalTransactions() + 1);

        if (transaction.getStatus() == TransactionStatus.Failed) {
            ledger.setTotalFailedAmount(ledger.getTotalFailedAmount()
                    .add(transactionAmount));
            ledger.setFailedTransactions(ledger.getFailedTransactions() + 1);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateUserStats(Ledger ledger, UserUpdate userUpdate) {
        ledger.setNoOfUsers(userUpdate.getTotalUsers());
        ledger.setNoOfBlockedUsers(userUpdate.getBlockedUsers());
    }

    @Value
    public static class TransactionUpdate {
        UpdateType type;
        Transaction transaction;
        boolean delete;
        Transaction oldTransaction;
        UserUpdate userUpdate;

        public TransactionUpdate (UpdateType type, Transaction transaction, boolean delete, Transaction oldTransaction, UserUpdate userUpdate) {
            this.type = type;
            this.transaction = transaction;
            this.delete = delete;
            this.oldTransaction = oldTransaction;
            this.userUpdate = userUpdate;
        }

        public UpdateType getType () {
            return type;
        }

        public Transaction getTransaction () {
            return transaction;
        }

        public boolean isDelete () {
            return delete;
        }

        public Transaction getOldTransaction () {
            return oldTransaction;
        }

        public UserUpdate getUserUpdate () {
            return userUpdate;
        }
    }

    @Value
    public static class UserUpdate {
        int totalUsers;
        int blockedUsers;

        public UserUpdate (int totalUsers, int blockedUsers) {
            this.totalUsers = totalUsers;
            this.blockedUsers = blockedUsers;
        }


        public int getTotalUsers () {
            return totalUsers;
        }

        public int getBlockedUsers () {
            return blockedUsers;
        }
    }

    public enum UpdateType {
        TRANSACTION,
        USER
    }


    @Transactional
    @Override
    public Ledger addLedger (Ledger ledger) {
        return ledgerDao.save(ledger);
    }

    @Transactional
    @Override
    public Ledger updateLedger (Ledger newLedger) {
        return ledgerDao.save(newLedger);

     }
     

}

