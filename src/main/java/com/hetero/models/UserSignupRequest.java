package com.hetero.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;


public class UserSignupRequest {

    @NotNull(message = "Email Cannot be Null")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotNull(message = "Password cannot be Null")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "MPin cannot be Null or Empty")
    @JsonProperty(value = "mPin", access = JsonProperty.Access.WRITE_ONLY)
    private String mPin;

    @NotNull(message = "Firstname cannot be Null")
    private String firstName;

    private String lastName;

    private String mobileNo;

    @Enumerated(EnumType.STRING)
    private Platform platformType = Platform.ALL;

    @Enumerated(EnumType.STRING)
    private Role role;


    public UserSignupRequest () {
    }

    public UserSignupRequest (String email, String password, String mPin, String firstName, String lastName, String mobileNo, Platform platformType, Role role) {
        this.email = email;
        this.password = password;
        this.mPin = mPin;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNo = mobileNo;
        this.platformType = platformType;
        this.role = role;
    }

    public @NotNull(message = "Email Cannot be Null") @Email(message = "Please provide a valid email") String getEmail () {
        return email;
    }

    public @NotNull(message = "Password cannot be Null") String getPassword () {
        return password;
    }

    @JsonProperty("mPin")
    public @NotNull(message = "MPin cannot be Null or Empty") String getMPin () {
        return mPin;
    }

    public @NotNull(message = "Firstname cannot be Null") String getFirstName () {
        return firstName;
    }

    public String getLastName () {
        return lastName;
    }

    public String getMobileNo () {
        return mobileNo;
    }

    public Platform getPlatformType () {
        return platformType;
    }

    public Role getRole () {
        return role;
    }
}
