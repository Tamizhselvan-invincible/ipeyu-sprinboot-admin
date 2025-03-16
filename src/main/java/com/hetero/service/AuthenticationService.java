package com.hetero.service;

import com.hetero.exception.InvalidException;
import com.hetero.exception.JWTTokenNotValid;
import com.hetero.exception.UserNotFoundException;
import com.hetero.models.Role;
import com.hetero.models.Token;
import com.hetero.repository.TokenDao;
import com.hetero.repository.UserDao;
import com.hetero.models.User;
import com.hetero.models.AuthenticationResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 *  If you want to modify the username password change the Email and mPin in Users Model
 *  Also made change in Authentication Service findByEmail(request.getUsername()).isPresent()
 *  and modify loadByUser in  JwtAuthenticationFilter
 */

@Service
public class AuthenticationService {

    private final UserDao userDao;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final JwtService jwtService;

    @Autowired
    private final TokenDao tokenRepository;

    @Autowired
    private final AuthenticationManager authenticationManager;



    public AuthenticationService(UserDao repository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 TokenDao tokenRepository,
                                 AuthenticationManager authenticationManager) {
        this.userDao = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(User request){

        if (request.getmPin() == null || request.getmPin().isEmpty()) {
            throw new IllegalArgumentException("M-PIN cannot be null or empty");
        }
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

      // check if user already exist. if exist than throw an Error
//        if(userDao.findByEmail(request.getUsername()).isPresent()) {
//            return new AuthenticationResponse(null, null,"User already exist");
//        }

         /** check if user already exist. if exist than authenticate the user **/
        if(userDao.findByEmail(request.getUsername()).isPresent()) {
            return this.authenticate(request);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getMobileNo() != null) user.setMobileNo(request.getMobileNo());
        if (request.getProfilePicture() != null) user.setProfilePicture(request.getProfilePicture());
        if (request.getAccountStatus() != null) user.setAccountStatus(request.getAccountStatus());
        if (request.getAppVersion() != null) user.setAppVersion(request.getAppVersion());
        if (request.getLastLoginTime() != null) user.setLastLoginTime(request.getLastLoginTime());
        if (request.getUserRole() != null){
            user.setUserRole(request.getUserRole());
        }
        else if (request.getUserRole() == null) {
           user.setUserRole(Role.USER);
        }

        if (request.getAppUpdatedAt() != null) user.setAppUpdatedAt(request.getAppUpdatedAt());
        if (request.getDeviceBrandName() != null) user.setDeviceBrandName(request.getDeviceBrandName());
        if (request.getDeviceVersionCode() != null) user.setDeviceVersionCode(request.getDeviceVersionCode());
        if (request.getOsType() != null) user.setOsType(request.getOsType());
        if(request.getTokens() != null)
            user.setTokens(request.getTokens());
        if(request.getEmailPassword() != null)
            user.setEmailPassword(request.getEmailPassword());


        user.setLastLoginTime(LocalDateTime.now());

        user = userDao.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(accessToken, refreshToken, user);

            return new AuthenticationResponse(accessToken, refreshToken, user, "User Register Successfully");

    }

    public AuthenticationResponse authenticate(User request) throws InvalidException{

        User user = userDao.findByEmail(request.getUsername()).orElseThrow(()->new UserNotFoundException("User "+request.getUsername()+" not found"));

        if(request.getEmailPassword() != null){
             if (!passwordEncoder.matches(request.getEmailPassword(), user.getEmailPassword())) {
                throw new InvalidException("User Email "+ request.getEmail() + " Entered Invalid Password");
            }
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        user.setLastLoginTime(LocalDateTime.now());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        userDao.save(user);
        revokeAllTokenByUser(user);
        saveUserToken(accessToken, refreshToken, user);
        return new AuthenticationResponse(accessToken, refreshToken, user,"User Logged In Successfully");

    }

    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllAccessTokensByUser(user.getId());
        if(validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t-> {
            t.setLoggedOut(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }

    public AuthenticationResponse refreshToken(
            HttpServletRequest request) {
        // extract the token from authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
          throw new JWTTokenNotValid("Unauthorized. Token not valid");
        }

        String token = authHeader.substring(7);

        // extract username from token
        String username = jwtService.extractUsername(token);

        // check if the user exist in database
        User user = userDao.findByEmail(username)
                .orElseThrow(()->new RuntimeException("No user found"));

        // check if the token is valid
        if(jwtService.isValidRefreshToken(token, user)) {
            // generate access token
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllTokenByUser(user);
            saveUserToken(accessToken, refreshToken, user);

            return new AuthenticationResponse(accessToken, refreshToken, user, "New token generated");
        }

        throw new JWTTokenNotValid("Token is Not Valid");

    }
}
