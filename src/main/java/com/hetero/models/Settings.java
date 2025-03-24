package com.hetero.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hetero.security.AESEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "settings")
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(name = "banner_name")
    @Convert(converter = AESEncryptor.class)
    private String bannerName;

    @Column(name = "app_name")
    @Convert(converter = AESEncryptor.class)
    private String appName;

    @Column(name = "app_logo")
    @Convert(converter = AESEncryptor.class)
    private String appLogo;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    public Settings () {
        this.createdAt = Instant.now().getEpochSecond();
        this.updatedAt = Instant.now().getEpochSecond();
    }

    public Settings (String bannerName, String appName, String appLogo) {
        this.bannerName = bannerName;
        this.appName = appName;
        this.appLogo = appLogo;
        this.createdAt = Instant.now().getEpochSecond();
        this.updatedAt = Instant.now().getEpochSecond();
    }

    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public String getBannerName () {
        return bannerName;
    }

    public void setBannerName (String bannerName) {
        this.bannerName = bannerName;
    }

    public String getAppName () {
        return appName;
    }

    public void setAppName (String appName) {
        this.appName = appName;
    }

    public String getAppLogo () {
        return appLogo;
    }

    public void setAppLogo (String appLogo) {
        this.appLogo = appLogo;
    }

    public Long getCreatedAt () {
        return createdAt;
    }

    public void setCreatedAt (Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt () {
        return updatedAt;
    }

    public void setUpdatedAt (Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}