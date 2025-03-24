package com.hetero.service;

import com.hetero.exception.UserNotFoundException;
import com.hetero.models.Platform;
import com.hetero.models.Token;
import com.hetero.models.Transaction;
import com.hetero.models.User;
import com.hetero.repository.TokenDao;
import com.hetero.repository.TransactionDao;
import com.hetero.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    TransactionDao transactionDao;

    @Autowired
    TokenDao tokenDao;

    @Transactional
    @Override
    public User addUser (User user) {
//
//        if(user.getMPin()==null){
//            SecureRandom secureRandom = new SecureRandom();
//            int randomMPin = 1000 + secureRandom.nextInt(9000);
//            user.setMPin(String.valueOf(randomMPin));
//
//        }
        return userDao.save(user);
    }



    @Transactional
    @Override
    public User updateUserBlockStatus(Long id, User updatedUser) {
        User existingUser = getUser(id);
        if (existingUser == null)
            throw  new UserNotFoundException("User with ID " + id + " not found");

        existingUser.setBlocked(updatedUser.isBlocked());
        return userDao.save(existingUser);
    }


    @Transactional
    @Override
    public List<Transaction> getUserTransactions(Long userId) {

        User existingUser = getUser(userId);
        if (existingUser == null)
            throw new UserNotFoundException("User with ID " + userId + " not found");

        List<Transaction> transactions = transactionDao.findByUserId(userId);
        if (transactions == null || transactions.isEmpty()) return new ArrayList<>();
        return transactions;
    }

    @Transactional
    @Override
    public void blockUser(Long userId) {
        User user = userDao.findById(userId).orElseThrow();
        user.setBlocked(true);
        userDao.save(user);
    }

    @Transactional
    @Override
    public void unblockUser(Long userId) {
        User user = userDao.findById(userId).orElseThrow();
        user.setBlocked(false);
        userDao.save(user);
    }

    @Transactional
    @Override
    public User updateUser(Long id, User newUser) {
        // Fetch the existing user from DB
        Optional<User> optionalUser = userDao.findById(id);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }

        User existingUser = optionalUser.get();

        // Update only non-null fields
        if (newUser.getFirstName() != null) existingUser.setFirstName(newUser.getFirstName());
        if (newUser.getLastName() != null) existingUser.setLastName(newUser.getLastName());
        if (newUser.getEmail() != null) existingUser.setEmail(newUser.getEmail());
        if (newUser.getMobileNo() != null) existingUser.setMobileNo(newUser.getMobileNo());
        if (newUser.getmPin() != null) existingUser.setmPin(newUser.getmPin());
        if (newUser.getProfilePicture() != null) existingUser.setProfilePicture(newUser.getProfilePicture());
        if (newUser.getAccountStatus() != null) existingUser.setAccountStatus(newUser.getAccountStatus());
        if (newUser.getAppVersion() != null) existingUser.setAppVersion(newUser.getAppVersion());
        if (newUser.getLastLoginTime() != null) existingUser.setLastLoginTime(newUser.getLastLoginTime());
        if (newUser.getUserRole() != null) existingUser.setUserRole(newUser.getUserRole());
        if (newUser.getAppUpdatedAt() != null) existingUser.setAppUpdatedAt(newUser.getAppUpdatedAt());
        if (newUser.getDeviceBrandName() != null) existingUser.setDeviceBrandName(newUser.getDeviceBrandName());
        if (newUser.getDeviceVersionCode() != null) existingUser.setDeviceVersionCode(newUser.getDeviceVersionCode());
        if (newUser.getOsType() != null) existingUser.setOsType(newUser.getOsType());
        if(newUser.getTokens() != null) existingUser.setTokens(newUser.getTokens());
        if(newUser.getCashBack() != null) existingUser.setCashBack(newUser.getCashBack());
        if(newUser.getEmailPassword() != null) existingUser.setEmailPassword(newUser.getEmailPassword());

        // Handle boolean field updates
        existingUser.setBlocked(newUser.isBlocked());

        // Handle transactions (if provided)
        if (newUser.getTransactions() != null && !newUser.getTransactions().isEmpty()) {
            for (Transaction transaction : newUser.getTransactions()) {
                transaction.setUserId(existingUser.getId()); // Ensure proper association
            }
            existingUser.getTransactions().clear();
            existingUser.getTransactions().addAll(newUser.getTransactions());
        }

        existingUser.setDateUpdated(Instant.now().getEpochSecond());
        // Save the updated user
        return userDao.save(existingUser);
    }



    @Transactional
    @Override
    public void updateUserTransactions (User user) {
        userDao.save(user);
    }


    @Transactional
    @Override
    public void updateUserCashBackTransactions(Long userId,Double cashBackAmount) {
       User user = userDao.findById(userId).orElseThrow(
               () -> new UserNotFoundException("User with ID " + userId + " not found")
       );
       String useCashback = user.getCashBack();
       if (useCashback == null)
           user.setCashBack(String.valueOf(cashBackAmount));
       else {
           Double cashBack = Double.parseDouble(user.getCashBack()) + cashBackAmount;
           user.setCashBack(String.valueOf(cashBack));
       }
       userDao.save(user);
    }

    @Transactional
    @Override
    public String deleteUser (Long id) {
        Optional<User> optionalUser = userDao.findById(id);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }else {
            User user = optionalUser.get();

            tokenDao.deleteAll(user.getTokens());

            userDao.delete(user);
            return "User with ID " + id + " deleted";
        }
    }

    @Transactional
    @Override
    public User getUser (Long id) {
        return userDao.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }




    @Transactional
    @Override
    public List<User> getAllUsers () {
        List<User> users = userDao.findAll();
        users.forEach(user -> user.getTransactions().size()); // Force initialization
        return users;
    }

    @Transactional
    @Override
    public List<User> getAllUnBlockedUsers() {
        return userDao.findByIsBlockedFalse();
    }

    @Transactional
    @Override
    public List<User> getAllBlockedUsers() {
        return userDao.findByIsBlockedTrue();
    }

    @Transactional
    @Override
    public List<User> getUsersByPlatform(Platform platform) {
        return userDao.findByPlatformType(platform);
    }

    @Transactional
    @Override
    public List<User> getUnblockedUsersByPlatform(Platform platform) {
        return userDao.findByPlatformTypeAndIsBlockedFalse(platform);
    }

    @Transactional
    @Override
    public List<User> getBlockedUsersByPlatform(Platform platform) {
        return userDao.findByPlatformTypeAndIsBlockedTrue(platform);
    }
}

