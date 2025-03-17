package com.hetero.controller;

import com.hetero.exception.UserNotFoundException;
import com.hetero.models.*;
import com.hetero.repository.UserDao;
import com.hetero.service.AuthenticationService;
import com.hetero.utils.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;


import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private final AuthenticationService authService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }


    @PostMapping("/authentication")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @RequestBody User request
            ) {
        if (request.getmPin() == null || request.getmPin().isEmpty()) {
            throw new IllegalArgumentException("M-PIN cannot be null or empty");
        }
        ApiResponse<AuthenticationResponse> response = new ApiResponse<>(202,"User Authenticated",
                authService.register(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request
    ) {
        ApiResponse<AuthenticationResponse> response = new ApiResponse<>(202,"Token Refreshed",authService.refreshToken(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserSignupRequest request) {

        if (request.getMPin() == null || request.getMPin().isEmpty()) {
            throw new IllegalArgumentException("M-PIN cannot be null or empty");
        }
        if (userDao.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered. Please SignIn");
        }

        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        User user = getUserFromSignupRequest(request, encryptedPassword);


        ApiResponse<AuthenticationResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "User Registered Successfully",
                authService.register(user)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }




    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(
            @RequestBody UserSignupRequest request
    ) {

        if (request.getMPin() == null || request.getMPin().isEmpty() ) {
            throw new IllegalArgumentException("M-PIN cannot be null or empty");
        }

        if (request.getEmail() == null || request.getEmail().isEmpty() ) {
            throw new IllegalArgumentException("M-PIN cannot be null or empty");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty() ) {
            throw new IllegalArgumentException("Sign In Password cannot be null or empty");
        }
        if (userDao.findByEmail(request.getEmail()).isEmpty()) {
            throw new RuntimeException("Email is Not registered. Please SignUp");
        }

        User user = getUserFromSignupRequest(request, request.getPassword());

        ApiResponse<AuthenticationResponse> response = new ApiResponse<>(
                HttpStatus.ACCEPTED.value(),
                "User Login Successful",
                authService. authenticate(user)
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody UserForgotPassword request
            ) {

        User user = userDao.findByEmail(request.getEmail()).orElseThrow(
                () -> new UserNotFoundException("Email "+ request.getEmail() +" not found")
        );

        if(user.getEmailPassword() == null || user.getEmailPassword().isEmpty()) {
            ApiResponse<User> response = new ApiResponse<>(
                    HttpStatus.NOT_ACCEPTABLE.value(),
                    "User Logged Via Google Account. User Password is Not Found",
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
        }

        User updatedUser = authService.forgotPassword(request,user);

        ApiResponse<User> response = new ApiResponse<>(
                HttpStatus.ACCEPTED.value(),
                "User Password has Updated",
                updatedUser
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @NotNull
    private static User getUserFromSignupRequest (UserSignupRequest request, String encryptedPassword) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setEmailPassword(encryptedPassword);
        user.setmPin(request.getMPin());
        if(request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getMobileNo() != null)
            user.setMobileNo(request.getMobileNo());
        if(request.getPlatformType() != null)
            user.setPlatformType(request.getPlatformType());
        if (request.getRole() != null)
            user.setUserRole(request.getRole());
        else
            user.setUserRole(Role.USER);
        return user;
    }
}