package com.hetero.controller;

import com.hetero.exception.UserNotFoundException;
import com.hetero.models.Platform;
import com.hetero.models.Transaction;
import com.hetero.models.User;
import com.hetero.service.LedgerService;
import com.hetero.service.LedgerServiceImpl;
import com.hetero.service.UserService;
import com.hetero.utils.ApiResponse;
import jakarta.validation.Valid;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://52.66.253.103")
public class UserController {
    private static final Logger log = LogManager.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private LedgerService ledgerService;

    ///POST MAPPINGS
    @PostMapping
    public ResponseEntity<ApiResponse<User>> addUser(@RequestBody @Valid User user) {
        User savedUser = userService.addUser(user);
        try {
            ledgerService.updateLedgerWithRetry(new LedgerServiceImpl.TransactionUpdate(
                    LedgerServiceImpl.UpdateType.USER,
                    null,
                    false,
                    null,
                    new LedgerServiceImpl.UserUpdate(
                            Math.max(ledgerService.getLedger().getNoOfUsers() + 1, userService.getAllUsers().size()),
                            user.isBlocked() ? ledgerService.getLedger().getNoOfBlockedUsers() + 1
                                    : ledgerService.getLedger().getNoOfBlockedUsers()
                    )
            ));
            ApiResponse<User> response = new ApiResponse<>(201, "User created successfully", savedUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ConcurrentModificationException e) {
            log.error("Failed to update ledger for new user: {}", savedUser.getId(), e);

            ApiResponse<User> errorResponse = new ApiResponse<>(409, "Conflict: Failed to update ledger", null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }


    @PostMapping("/block")
    public ResponseEntity<?> blockUser(@RequestParam Long userId) {
        User user = userService.getUser(userId);
        if (user == null || user.isBlocked()) {
            return ResponseEntity.badRequest().body("User Not Found or Already Blocked");
        }

        try {
            userService.blockUser(userId);
            ledgerService.updateLedgerWithRetry(new LedgerServiceImpl.TransactionUpdate(
                    LedgerServiceImpl.UpdateType.USER,
                    null,
                    false,
                    null,
                    new LedgerServiceImpl.UserUpdate(
                            ledgerService.getLedger().getNoOfUsers(),
                            ledgerService.getLedger().getNoOfBlockedUsers() + 1
                    )
            ));
            ApiResponse<User> response = new ApiResponse<>(201, "User Blocked successfully", null);
            return ResponseEntity.ok().body(response);
        } catch (ConcurrentModificationException e) {
            log.error("Failed to update ledger for blocking user: {}", userId, e);
            ApiResponse<User> errorResponse = new ApiResponse<>(409, "Conflict: Failed to Block User: "+ e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @PostMapping("/unblock")
    public ResponseEntity<?> unblockUser(@RequestParam Long userId) {
        User user = userService.getUser(userId);
        if (user == null || !user.isBlocked()) {
            return ResponseEntity.badRequest().body("User Not Found or Already Unblocked");
        }

        try {
            userService.unblockUser(userId);
            ledgerService.updateLedgerWithRetry(new LedgerServiceImpl.TransactionUpdate(
                    LedgerServiceImpl.UpdateType.USER,
                    null,
                    false,
                    null,
                    new LedgerServiceImpl.UserUpdate(
                            ledgerService.getLedger().getNoOfUsers(),
                            ledgerService.getLedger().getNoOfBlockedUsers() - 1
                    )
            ));
            ApiResponse<User> response = new ApiResponse<>(201, "User Unblocked successfully", null);
            return ResponseEntity.ok().body(response);
        } catch (ConcurrentModificationException e) {
            log.error("Failed to update ledger for unblocking user: {}", userId, e);
            ApiResponse<User> errorResponse = new ApiResponse<>(409, "Conflict: Failed to UnBlock User: "+ e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }


    /// Delete Mapping

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }


        try {
            ledgerService.updateLedgerWithRetry(new LedgerServiceImpl.TransactionUpdate(
                    LedgerServiceImpl.UpdateType.USER,
                    null,
                    false,
                    null,
                    new LedgerServiceImpl.UserUpdate(
                            Math.max(ledgerService.getLedger().getNoOfUsers() - 1, 0),
                            user.isBlocked() ? ledgerService.getLedger().getNoOfBlockedUsers() - 1
                                    : ledgerService.getLedger().getNoOfBlockedUsers()
                    )
            ));

            String message = userService.deleteUser(id);
            ApiResponse<User> response = new ApiResponse<>(201, message, null);
            return ResponseEntity.ok().body(response);

        } catch (ConcurrentModificationException e) {
            log.error("Failed to update ledger for user deletion: {}", id, e);
            ApiResponse<User> response = new ApiResponse<>(201, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }


    ///Put Mapping

    // Read operations - unchanged
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existingUser = userService.getUser(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        ApiResponse<User> response = new ApiResponse<>(200, "User updated successfully", userService.updateUser(id, user));
        return ResponseEntity.ok(response);
    }


    ///GET MAPPINGS

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserTransactions(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/unblocked")
    public ResponseEntity<List<User>> getAllUnBlockedUsers() {
        return ResponseEntity.ok(userService.getAllUnBlockedUsers());
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<User>> getAllBlockedUsers() {
        return ResponseEntity.ok(userService.getAllBlockedUsers());
    }


    ///GET Mapping For Platform Specific Fetching

       @PostMapping("/platform/{platform}")
        public ResponseEntity<List<User>> getUsersByPlatform(@PathVariable Platform platform) {
            return ResponseEntity.ok(userService.getUsersByPlatform(platform));
        }

        @PostMapping("/platform/{platform}/unblocked")
        public ResponseEntity<List<User>> getUnblockedUsersByPlatform(@PathVariable Platform platform) {
            return ResponseEntity.ok(userService.getUnblockedUsersByPlatform(platform));
        }

        @PostMapping("/platform/{platform}/blocked")
        public ResponseEntity<List<User>> getBlockedUsersByPlatform(@PathVariable Platform platform) {
            return ResponseEntity.ok(userService.getBlockedUsersByPlatform(platform));
        }


}

