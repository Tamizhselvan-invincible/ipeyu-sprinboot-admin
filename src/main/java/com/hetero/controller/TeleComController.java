package com.hetero.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hetero.models.*;
import com.hetero.models.telecom.BillPaymentRequest;
import com.hetero.service.TelecomScrizaAPIService;
import com.hetero.service.TransactionService;
import com.hetero.service.UserService;
import com.hetero.utils.ApiErrorResponse;
import com.hetero.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/telecom")
@CrossOrigin(origins = "http://52.66.253.103")
public class TeleComController {

    private static final Logger log = LoggerFactory.getLogger(TeleComController.class);
    @Autowired
    TelecomScrizaAPIService telecomService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    /** * Mobile DTH End Points */

    @PostMapping("/balance")
    public ResponseEntity<?> getBalanceFromAPIService(){
        return telecomService.getBalanceAmount();
    }

    @PostMapping("/recharge-payment")
    public ResponseEntity<?> processRecharge(
            @RequestParam String mobileNo,
            @RequestParam String amount,
            @RequestParam String providerId,
            @RequestParam String clientId,
            @RequestParam double cashback,
            @RequestParam PaymentMethod paymentMethod,
            @RequestParam Platform platform
    )
    {

        Long userId = Long.parseLong(clientId);

        if (userService.getUser(userId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiErrorResponse<>(404,"User Not found", "Null Pointer Exception",null)
            );
        }

        String data = telecomService.rechargePayment(mobileNo,amount,providerId,clientId);
        JsonNode jsonData;
        try {
            jsonData = objectMapper.readTree(data);

            String status = jsonData.get("status").asText();
            String payId = jsonData.get("payid").asText();

            SubscriptionPlan subscriptionPlan = new SubscriptionPlan();


            subscriptionPlan.setAmount(BigDecimal.valueOf(Double.parseDouble(amount)));

            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setCashBack(String.valueOf(cashback));
            transaction.setPaymentMethod(paymentMethod);
            transaction.setUserId(userId);
            if (status.equals("success")) {
                transaction.setStatus(TransactionStatus.Success);
            } else if (status.equals("failure")) {
                transaction.setStatus(TransactionStatus.Failed);
            }
            transaction.setAggregatedTransactionId(Long.parseLong(payId));
            transaction.setPlatformType(platform);
            transaction.setSubscriptionPlan(subscriptionPlan);

            userService.updateUserCashBackTransactions(userId,cashback);
            transactionService.addTransaction(transaction);

        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiErrorResponse<>(500, "Error parsing JSON", e.getMessage(),null));
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiErrorResponse<>(503, "Error parsing JSON", e.getMessage(),null));
        }
        ApiResponse<JsonNode> apiResponse = new ApiResponse<>(202,"Recharge Successful", jsonData);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponse);
    }



    /** Recharge Plan End Points */
    @PostMapping("/prepaid_plans")
    public ResponseEntity<?> getPrepaidPlansFromAPIService1(
            @RequestParam String providerId,
            @RequestParam String stateId) {

        return telecomService.getPlansService1(providerId,stateId);
    }

    @PostMapping("/prepaid_plans2")
    public ResponseEntity<?> getPrepaidPlansFromAPIService2(
            @RequestParam String providerId,
            @RequestParam String stateId) {

        return telecomService.getPlansService2(providerId,stateId);
    }

    @PostMapping("/r-offer")
    public ResponseEntity<?> getRofferPlanFromService(
            @RequestParam String providerId,
            @RequestParam String mobileNo) {

        System.out.println("Before Call in Controller: ");
        return telecomService.getRofferPlan(providerId,mobileNo);
    }

    @PostMapping("/dth-plans")
    public ResponseEntity<?> getDTHPlanFromService(
            @RequestParam String providerId) {
        return telecomService.getDTHPlans(providerId);
    }

    @PostMapping("/find-operator")
    public ResponseEntity<?> getOperatorFromService(
            @RequestParam String mobileNo) {
        return telecomService.findOperator(mobileNo);
    }

    @PostMapping("/state-list")
    public ResponseEntity<?> getStateListFromService() {
        return telecomService.getStateList();
    }


    /* ** Bill Payment End Points */

    @PostMapping("/get-provider")
    public ResponseEntity<?> getProvidersListFormService() {
        return  telecomService.getProvidersList();
    }

    @PostMapping("/bill-payment")
    public ResponseEntity<?> makePayment(@RequestBody BillPaymentRequest request) throws IOException {
       return telecomService.processBillPayment(request);
    }



}
