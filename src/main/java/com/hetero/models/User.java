package com.hetero.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hetero.security.AESEncryptor;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;


@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "First Name cannot be NULL")
    @Pattern(regexp = "[A-Za-z.\\s]+", message = "Enter valid characters in first name")
    @Column(name = "first_name", length = 50)
    @Convert(converter = AESEncryptor.class)
    private String firstName;

    @Pattern(regexp = "[A-Za-z.\\s]+", message = "Enter valid characters in last name")
    @Column(name = "last_name", length = 50)
    @Convert(converter = AESEncryptor.class)
    private String lastName;

    @NotNull(message = "Email cannot be NULL")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "[6789]{1}[0-9]{9}", message = "Enter valid 10 digit mobile number")
    @Column(name = "mobile_number",unique = true,nullable = true)
    @Convert(converter = AESEncryptor.class)
    private String mobileNo;

    @NotNull(message = "M-PIN cannot be null")
    @Column(name = "m_pin", length = 100)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String mPin;

    @Column(name = "profile_picture")
    @Convert(converter = AESEncryptor.class)
    private String profilePicture;

    @Column(name = "is_blocked")
    private boolean isBlocked;

    @Column(name = "account_status")
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date dateCreated;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date dateUpdated;

    @Column(name = "cashback_amount")
    @Convert(converter = AESEncryptor.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String cashBack;

    @Column(name = "card_number")
    @Convert(converter = AESEncryptor.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String cardNumber;


    @Column(name = "card_type")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private CardType cardType;

    @Column
    @Enumerated(EnumType.STRING)
    private Platform platformType = Platform.ALL;

    @Column(name = "deleted_at",nullable = true)
    private Date deletedAt;

    @Column(name = "app_version")
    String appVersion;

    @Column(name = "last_login_time")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastLoginTime;

    @Column(name = "user_role", columnDefinition = "VARCHAR(20) CHECK (user_role IN ('USER', 'ADMIN', 'PROVIDER'))")
    @Enumerated(EnumType.STRING)
    Role userRole;

    @Column(name = "app_updated_at")
    private Date appUpdatedAt;

    @Column(name = "device_brand_name")
    private String deviceBrandName;

    @Column(name = "device_version_code")
    private String deviceVersionCode;

    @Column(name = "os_type")
    private String osType;

    @Column(name = "life_time_earning")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String lifeTimeEarning;

    @OneToMany(mappedBy = "userId")
    @JsonIgnore
    @JsonManagedReference
    private List<Transaction> transactions = new ArrayList<>();


    @Column(name = "password")
    @JsonIgnore
    private String emailPassword;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Token> tokens;

    ///User Details Properties
    @JsonIgnore
    private boolean enabled = true;

    @JsonIgnore
    private boolean credentialsNonExpired = true;

    @JsonIgnore
    private boolean accountNonExpired = true;

    @JsonIgnore
    private boolean accountNonLocked = true;


    public User () {
        this.cardType = CardType.BASIC;
        this.dateCreated = new Date();
        this.dateUpdated = new Date();
        this.cardNumber = generateUniqueCardNumber();
        this.cashBack = "0.0";
        this.lifeTimeEarning = "0.0";
    }

    public String getLifeTimeEarning () {
        return lifeTimeEarning;
    }

    public void setLifeTimeEarning (String lifeTimeEarning) {
        this.lifeTimeEarning = lifeTimeEarning;
    }

    public User (
            String firstName,
            String lastName,
            String email,
            String mobileNo,
            String mPin,
            String profilePicture,
            boolean isBlocked,
            AccountStatus accountStatus,
            Date dateUpdated,
            Platform platformType,
            Date deletedAt,
            String appVersion,
            LocalDateTime lastLoginTime,
            Role userRole,
            Date appUpdatedAt,
            String deviceBrandName,
            String deviceVersionCode,
            String osType,
            List<Transaction> transactions,
            List<Token> tokens) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobileNo = mobileNo;
        this.mPin = mPin;
        this.profilePicture = profilePicture;
        this.isBlocked = isBlocked;
        this.accountStatus = accountStatus;
        this.dateCreated = new Date();
        this.dateUpdated = dateUpdated;
        this.platformType = platformType;
        this.deletedAt = deletedAt;
        this.appVersion = appVersion;
        this.lastLoginTime = lastLoginTime;
        this.userRole = userRole;
        this.appUpdatedAt = appUpdatedAt;
        this.deviceBrandName = deviceBrandName;
        this.deviceVersionCode = deviceVersionCode;
        this.osType = osType;
        this.transactions = transactions;
        this.tokens = tokens;
        this.cardNumber = generateUniqueCardNumber();
        this.cardType = CardType.BASIC;
        this.cashBack = "0.0";
        this.lifeTimeEarning = "0.0";
    }



    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities () {
        return List.of(new SimpleGrantedAuthority(userRole.name()));
    }

    @JsonIgnore
    @Override
    public String getPassword () {
        return mPin;
    }

    public void setPassword (String password) {
         this.mPin = password;
    }
    @JsonIgnore
    public String getEmailPassword () {
        return emailPassword;
    }

    public void setEmailPassword (String emailPassword) {
        this.emailPassword = emailPassword;
    }

    @JsonIgnore
    @Override
    public String getUsername () {
        return this.email;
    }

    public void setUsername(String username) {
        this.email = username;
    }

    public @NotNull(message = "First Name cannot be NULL") @Pattern(regexp = "[A-Za-z.\\s]+", message = "Enter valid characters in first name") String getFirstName () {
        return firstName;
    }

    public void setFirstName (@NotNull(message = "First Name cannot be NULL") @Pattern(regexp = "[A-Za-z.\\s]+", message = "Enter valid characters in first name") String firstName) {
        this.firstName = firstName;
    }

    public @Pattern(regexp = "[A-Za-z.\\s]+", message = "Enter valid characters in last name") String getLastName () {
        return lastName;
    }

    public void setLastName (@Pattern(regexp = "[A-Za-z.\\s]+", message = "Enter valid characters in last name") String lastName) {
        this.lastName = lastName;
    }

    public @NotNull(message = "Email cannot be NULL") @Email(message = "Please provide a valid email address") String getEmail () {
        return email;
    }

    public void setEmail (@NotNull(message = "Email cannot be NULL") @Email(message = "Please provide a valid email address") String email) {
        this.email = email;
    }

    public String getCashBack () {
        return cashBack;
    }

    public void setCashBack (String cashBack) {
        this.cashBack = cashBack;
    }

    public String getCardNumber () {
        return cardNumber;
    }

    public void setCardNumber (String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public CardType getCardType () {
        return cardType;
    }

    public void setCardType (CardType cardType) {
        this.cardType = cardType;
    }

    public @Pattern(regexp = "[6789]{1}[0-9]{9}", message = "Enter valid 10 digit mobile number") String getMobileNo () {
        return mobileNo;
    }

    public void setMobileNo (@Pattern(regexp = "[6789]{1}[0-9]{9}", message = "Enter valid 10 digit mobile number") String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public @NotNull(message = "M-PIN cannot be null") String getmPin () {
        return mPin;
    }

    public void setmPin (@NotNull(message = "M-PIN cannot be null") String mPin) {
        this.mPin = mPin;
    }

    public String getProfilePicture () {
        return profilePicture;
    }

    public void setProfilePicture (String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isBlocked () {
        return isBlocked;
    }

    public void setBlocked (boolean blocked) {
        isBlocked = blocked;
    }

    public AccountStatus getAccountStatus () {
        return accountStatus;
    }

    public void setAccountStatus (AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Date getDateCreated () {
        return dateCreated;
    }

    public void setDateCreated (Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated () {
        return dateUpdated;
    }

    public void setDateUpdated (Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Platform getPlatformType () {
        return platformType;
    }

    public void setPlatformType (Platform platformType) {
        this.platformType = platformType;
    }

    public Date getDeletedAt () {
        return deletedAt;
    }

    public void setDeletedAt (Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getAppVersion () {
        return appVersion;
    }

    public void setAppVersion (String appVersion) {
        this.appVersion = appVersion;
    }

    public LocalDateTime getLastLoginTime () {
        return lastLoginTime;
    }

    public void setLastLoginTime (LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Role getUserRole () {
        return userRole;
    }

    public void setUserRole (Role userRole) {
        this.userRole = userRole;
    }

    public Date getAppUpdatedAt () {
        return appUpdatedAt;
    }

    public void setAppUpdatedAt (Date appUpdatedAt) {
        this.appUpdatedAt = appUpdatedAt;
    }

    public String getDeviceBrandName () {
        return deviceBrandName;
    }

    public void setDeviceBrandName (String deviceBrandName) {
        this.deviceBrandName = deviceBrandName;
    }

    public String getDeviceVersionCode () {
        return deviceVersionCode;
    }

    public void setDeviceVersionCode (String deviceVersionCode) {
        this.deviceVersionCode = deviceVersionCode;
    }

    public String getOsType () {
        return osType;
    }

    public void setOsType (String osType) {
        this.osType = osType;
    }

    public List<Transaction> getTransactions () {
        return transactions;
    }

    public void setTransactions (List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Token> getTokens () {
        return tokens;
    }

    public void setTokens (List<Token> tokens) {
        this.tokens = tokens;
    }

    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }


    public static String generateUniqueCardNumber() {
        String prefix = "IP";
        // Generate UUID and extract only the required number of characters
        String uuid = UUID.randomUUID().toString().replace("-", ""); // Remove hyphens
        String uniqueDigits = uuid.substring(0, 12); // Take first 12 characters

        // Format as IPXXXX-XXXX-XXXX
        return prefix + uniqueDigits.substring(0, 4) + "-" + uniqueDigits.substring(4, 8) + "-" + uniqueDigits.substring(8, 12);
    }
}