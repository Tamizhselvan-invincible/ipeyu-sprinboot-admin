package com.hetero.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hetero.security.AESEncryptor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(name = "plan_name")
    private String name;

    private String operator;

    @NotNull(message = "Amount is required")
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

//    @NotNull(message = "Validity is required")
    private Integer validity;

    @Column(name = "data_limit")
    private String dataLimit;

    @Column(name = "call_benefits")
    private String callBenefits;

    @Column(name = "sms_benefits")
    private String smsBenefits;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @CreationTimestamp
    private Date dateCreated;

    public SubscriptionPlan () {
        this.dateCreated = new Date();
    }

    public SubscriptionPlan ( String name, String operator, BigDecimal amount, Integer validity, String dataLimit, String callBenefits, String smsBenefits, String description, boolean isActive) {
        this.name = name;
        this.operator = operator;
        this.amount = amount;
        this.validity = validity;
        this.dataLimit = dataLimit;
        this.callBenefits = callBenefits;
        this.smsBenefits = smsBenefits;
        this.description = description;
        this.isActive = isActive;
        this.dateCreated = new Date();
    }

    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public  String getOperator () {
        return operator;
    }

    public void setOperator ( String operator) {
        this.operator = operator;
    }

    public @NotNull(message = "Amount is required") BigDecimal getAmount () {
        return amount;
    }

    public void setAmount (@NotNull(message = "Amount is required") BigDecimal amount) {
        this.amount = amount;
    }

    public  Integer getValidity () {
        return validity;
    }

    public void setValidity ( Integer validity) {
        this.validity = validity;
    }

    public String getDataLimit () {
        return dataLimit;
    }

    public void setDataLimit (String dataLimit) {
        this.dataLimit = dataLimit;
    }

    public String getCallBenefits () {
        return callBenefits;
    }

    public void setCallBenefits (String callBenefits) {
        this.callBenefits = callBenefits;
    }

    public String getSmsBenefits () {
        return smsBenefits;
    }

    public void setSmsBenefits (String smsBenefits) {
        this.smsBenefits = smsBenefits;
    }

    public String getDescription () {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public boolean isActive () {
        return isActive;
    }

    public void setActive (boolean active) {
        isActive = active;
    }

    public Date getDateCreated () {
        return dateCreated;
    }

    public void setDateCreated (Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
