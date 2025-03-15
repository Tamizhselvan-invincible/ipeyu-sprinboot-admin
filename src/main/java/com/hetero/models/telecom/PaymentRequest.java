package com.hetero.models.telecom;

import com.hetero.models.PaymentMethod;
import com.hetero.models.Platform;
import com.hetero.models.SubscriptionPlan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class PaymentRequest {

    @NotNull(message = "Mobile Number Cannot be Null")
    @Pattern(regexp = "[6789]{1}[0-9]{9}", message = "Enter valid 10 digit mobile number")
    private String mobileNo;

    @NotNull(message = "Amount Cannot be Null")
    @Valid
    private Double amount;

    @NotNull(message = "Provider Id Cannot be Null")
    private String providerId;

    @NotNull(message = "User Id Cannot be Null")
    private Long userID;


    @Valid
    private Double cashback;

    @NotNull(message = "Mobile Number Cannot be Null")
    @Valid
    private PaymentMethod paymentMethod;


    @Valid
    private Platform platform;

    private String txnImage;

    private String txnUserName;

    private Long txnUserId;

    private String txnStatus;

    String typeOfTransaction;

    @Valid
    SubscriptionPlan subscriptionPlan;

    @NotNull(message = "Environment Cannot be Null")
    @Pattern(regexp = "^(UAT|LIVE)$", message = "Environment must be either UAT or LIVE")
    private String environment;


    public PaymentRequest (
            String mobileNo,
            Double amount,
            String providerId,
            Long userID,
            Double cashback,
            PaymentMethod paymentMethod,
            Platform platform,
            String txnImage,
            String txnUserName,
            Long txnUserId,
            String txnStatus,
            String typeOfTransaction,
            SubscriptionPlan subscriptionPlan,
            String environment
    ) {
        this.mobileNo = mobileNo;
        this.amount = amount;
        this.providerId = providerId;
        this.userID = userID;
        this.cashback = cashback;
        this.paymentMethod = paymentMethod;
        this.platform = platform;
        this.txnImage = txnImage;
        this.txnUserName = txnUserName;
        this.txnUserId = txnUserId;
        this.txnStatus = txnStatus;
        this.typeOfTransaction = typeOfTransaction;
        this.subscriptionPlan = subscriptionPlan;
        this.environment = environment;
    }


    public @Pattern(regexp = "[6789]{1}[0-9]{9}", message = "Enter valid 10 digit mobile number") String getMobileNo () {
        return mobileNo;
    }

    public void setMobileNo (@Pattern(regexp = "[6789]{1}[0-9]{9}", message = "Enter valid 10 digit mobile number") String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public @Valid Double getAmount () {
        return amount;
    }

    public void setAmount (@Valid Double amount) {
        this.amount = amount;
    }

    public String getProviderId () {
        return providerId;
    }

    public void setProviderId (String providerId) {
        this.providerId = providerId;
    }

    public Long getUserID () {
        return userID;
    }

    public void setUserID (Long userID) {
        this.userID = userID;
    }

    public String getEnvironment () {
        return environment;
    }

    public void setEnvironment (String environment) {
        this.environment = environment;
    }

    public @Valid Double getCashback () {
        return cashback;
    }

    public void setCashback (@Valid Double cashback) {
        this.cashback = cashback;
    }

    public @Valid PaymentMethod getPaymentMethod () {
        return paymentMethod;
    }

    public void setPaymentMethod (@Valid PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public @Valid Platform getPlatform () {
        return platform;
    }

    public void setPlatform (@Valid Platform platform) {
        this.platform = platform;
    }

    public String getTxnImage () {
        return txnImage;
    }

    public void setTxnImage (String txnImage) {
        this.txnImage = txnImage;
    }

    public String getTxnUserName () {
        return txnUserName;
    }

    public void setTxnUserName (String txnUserName) {
        this.txnUserName = txnUserName;
    }

    public Long getTxnUserId () {
        return txnUserId;
    }

    public void setTxnUserId (Long txnUserId) {
        this.txnUserId = txnUserId;
    }

    public String getTxnStatus () {
        return txnStatus;
    }

    public void setTxnStatus (String txnStatus) {
        this.txnStatus = txnStatus;
    }

    public @Valid String getTypeOfTransaction () {
        return typeOfTransaction;
    }

    public void setTypeOfTransaction (@Valid String typeOfTransaction) {
        this.typeOfTransaction = typeOfTransaction;
    }

    public @Valid SubscriptionPlan getSubscriptionPlan () {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan (@Valid SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }
}
