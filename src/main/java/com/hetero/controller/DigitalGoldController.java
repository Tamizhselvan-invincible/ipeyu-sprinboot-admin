package com.hetero.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hetero.models.digigold.DigitalGoldProfileCreateRequest;
import com.hetero.models.digigold.DigitalGoldProfileUpdateRequest;
import com.hetero.service.PSDigitalGoldService;
import com.hetero.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/digitalgold")
@CrossOrigin(origins = "http://52.66.253.103")
public class DigitalGoldController {

    @Autowired
    private PSDigitalGoldService digitalGoldService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/profile")
    public ResponseEntity<?> getUserProfileFromService(@RequestParam  long mobileNo){

        String data = digitalGoldService.getProfile(mobileNo);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Profile Retrieved", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping("/profile/create")
    public ResponseEntity<?> createProfileForDigitalGoldFromService(@RequestBody DigitalGoldProfileCreateRequest requestDto){

        return digitalGoldService.createProfileForDigitalGold(requestDto);
//        JsonNode jsonData;
//        try {
//            jsonData = objectMapper.readTree(data);
//        } catch (JsonProcessingException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
//        }
//
//        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Profile Created", jsonData);
//        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping("/balance")
    public ResponseEntity<?> getBalanceFromService(@RequestParam String customerId){

        String data = digitalGoldService.getBalance(customerId);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Balance Retrieved", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activateAccFromService(@RequestParam String customerId){

        String data = digitalGoldService.activateUserAccount(customerId);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Account activated", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }



    @PostMapping("/deactivate")
    public ResponseEntity<?> deActivateAccFromService(@RequestParam String customerId){

        String data = digitalGoldService.deActivateUserAccount(customerId);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Account deactivated", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }


    @PostMapping("/profile/update")
    public ResponseEntity<?> updateProfileDetailsFromService(@RequestBody DigitalGoldProfileUpdateRequest requestDto){

        String data = digitalGoldService.updateProfileForDigitalGold(requestDto);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Profile Updated", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }


    @PostMapping("/quotation/get")
    public ResponseEntity<?> getQuotationFromService(@RequestParam String customerId,
                                                     @RequestParam String amount,
                                                     @RequestParam String quantity){
        String data = digitalGoldService.getQuotations(customerId, amount, quantity);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Quotations Retrieved", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping("/quotation/validate")
    public ResponseEntity<?> validateQuotationFromService(@RequestParam String customerId,
                                                     @RequestParam String billingAddressId,
                                                     @RequestParam String quoteId){
        String data = digitalGoldService.validateQuotations(customerId, billingAddressId, quoteId);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Quotations Validation Info", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }


    @PostMapping("/send_otp")
    public ResponseEntity<?> sendOTPFromService(
            @RequestParam  String refId,
            @RequestParam String customerId,
            @RequestParam String billingAddressId,
            @RequestParam String quoteId){

        String data = digitalGoldService.sendOTPToCustomer(refId, customerId, billingAddressId, quoteId);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "OTP Sent Info", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }


    @PostMapping("/buy_execute")
    public ResponseEntity<?> buyExecuteFromService(
           @RequestParam String refId,
           @RequestParam String customerId,
           @RequestParam String billingAddressId,
           @RequestParam String quoteId,
           @RequestParam String stateResp,
           @RequestParam Double otp){

        String data = digitalGoldService.buyExecute(refId, customerId, billingAddressId, quoteId, stateResp, otp);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Gold/Silver Buy Info", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }


    @PostMapping("/status")
    public ResponseEntity<?> transactionStatusFromService(@RequestParam String refId){

        String data = digitalGoldService.transactionStatus(refId);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Error parsing JSON", null));
        }

        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(200, "Transaction Status", jsonData);
        return ResponseEntity.ok().body(apiResponse);
    }

}
