package com.hetero.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class UserForgotPassword {

    @NotNull(message = "Email Cannot be Null")
    private String email;

    @NotNull(message = "Old Password cannot be Null")
    private String oldPassword;

    @NotNull(message = "New Password cannot be Null")
    private String newPassword;

    public UserForgotPassword (String email, String oldPassword, String newPassword) {
        this.email = email;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public @NotNull(message = "Email Cannot be Null") String getEmail () {
        return email;
    }

    public void setEmail (@NotNull(message = "Email Cannot be Null") String email) {
        this.email = email;
    }

    public @NotNull(message = "Old Password cannot be Null") String getOldPassword () {
        return oldPassword;
    }

    public void setOldPassword (@NotNull(message = "Old Password cannot be Null") String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public @NotNull(message = "New Password cannot be Null") String getNewPassword () {
        return newPassword;
    }

    public void setNewPassword (@NotNull(message = "New Password cannot be Null") String newPassword) {
        this.newPassword = newPassword;
    }
}

