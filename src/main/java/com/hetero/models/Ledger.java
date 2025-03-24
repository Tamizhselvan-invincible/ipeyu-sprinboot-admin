package com.hetero.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Entity
@Table(name = "ledger")
public class Ledger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;


    @Version
    private Integer version;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_failed_amount", precision = 10, scale = 2)
    private BigDecimal totalFailedAmount;

    @Column(name = "total_transactions")
    private Integer totalTransactions;

    @Column(name = "failed_transactions")
    private Integer failedTransactions;

    @Column(name = "total_users")
    private Integer noOfUsers;

    @Column(name = "blocked_users")
    private Integer noOfBlockedUsers;

    @Column(name = "stats_date")
    private Long statsDate;

    public Ledger () {
    }

    public Ledger (Long id, Integer version, BigDecimal totalAmount, BigDecimal totalFailedAmount, Integer totalTransactions, Integer failedTransactions, Integer noOfUsers, Integer noOfBlockedUsers, Long statsDate) {
        this.id = id;
        this.version = version;
        this.totalAmount = totalAmount;
        this.totalFailedAmount = totalFailedAmount;
        this.totalTransactions = totalTransactions;
        this.failedTransactions = failedTransactions;
        this.noOfUsers = noOfUsers;
        this.noOfBlockedUsers = noOfBlockedUsers;
        this.statsDate = statsDate;
    }

    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public Integer getVersion () {
        return version;
    }

    public void setVersion (Integer version) {
        this.version = version;
    }

    public BigDecimal getTotalAmount () {
        return totalAmount;
    }

    public void setTotalAmount (BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalFailedAmount () {
        return totalFailedAmount;
    }

    public void setTotalFailedAmount (BigDecimal totalFailedAmount) {
        this.totalFailedAmount = totalFailedAmount;
    }

    public Integer getTotalTransactions () {
        return totalTransactions;
    }

    public void setTotalTransactions (Integer totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Integer getFailedTransactions () {
        return failedTransactions;
    }

    public void setFailedTransactions (Integer failedTransactions) {
        this.failedTransactions = failedTransactions;
    }

    public Integer getNoOfUsers () {
        return noOfUsers;
    }

    public void setNoOfUsers (Integer noOfUsers) {
        this.noOfUsers = noOfUsers;
    }

    public Integer getNoOfBlockedUsers () {
        return noOfBlockedUsers;
    }

    public void setNoOfBlockedUsers (Integer noOfBlockedUsers) {
        this.noOfBlockedUsers = noOfBlockedUsers;
    }

    public Long getStatsDate () {
        return statsDate;
    }

    public void setStatsDate (Long statsDate) {
        this.statsDate = statsDate;
    }

    public LocalDateTime getFormattedStatsTime() {
        String dateStr = String.valueOf(statsDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(dateStr, formatter);
    }
}