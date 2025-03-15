package com.hetero.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @CreationTimestamp
    private LocalDateTime timestamp;



    public AuditLog () {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog (String userEmail, Transaction transaction, LocalDateTime timestamp) {
        this.userEmail = userEmail;
        this.transaction = transaction;
        this.timestamp = timestamp;
    }


    public Transaction getTransaction () {
        return transaction;
    }

    public void setTransaction (Transaction transaction) {
        this.transaction = transaction;
    }

    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public String getUserEmail () {
        return userEmail;
    }

    public void setUserEmail (String userEmail) {
        this.userEmail = userEmail;
    }

    public LocalDateTime getTimestamp () {
        return timestamp;
    }

    public void setTimestamp (LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
