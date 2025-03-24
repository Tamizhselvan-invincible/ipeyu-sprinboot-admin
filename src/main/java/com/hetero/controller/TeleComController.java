package com.hetero.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hetero.models.telecom.BillPaymentRequest;
import com.hetero.models.telecom.PaymentRequest;
import com.hetero.service.TelecomScrizaAPIService;
import com.hetero.service.TransactionService;
import com.hetero.service.UserService;
import com.hetero.utils.ApiErrorResponse;
import com.hetero.utils.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/telecom")
@CrossOrigin(origins = {"http://52.66.253.103", "http://localhost:8008"})
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
    public ResponseEntity<?> processRecharge(@Valid  @RequestBody PaymentRequest paymentRequest)
    {
        if (userService.getUser(paymentRequest.getUserID()) == null) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiErrorResponse<>(404,"User Not found", "Null Pointer Exception",null));
       }
        if (paymentRequest.getEnvironment().equals("LIVE"))
         if(!telecomService.verifyBalanceAmount(paymentRequest.getAmount())){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
              new ApiErrorResponse<>(HttpStatus.SERVICE_UNAVAILABLE.value(), "Amount Not Sufficient To Make the Recharge", "Invalid Amount",null)
            );
        }
        String data = telecomService.rechargePayment(
                paymentRequest.getMobileNo(),
                String.valueOf(paymentRequest.getAmount()),
                paymentRequest.getProviderId(),
                String.valueOf(paymentRequest.getUserID()),
                paymentRequest.getEnvironment()
        );
    if (data == null || data.trim().isEmpty()) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiErrorResponse<>(503, "No Response from Telecom Service", "Empty response received", null));
    }

    JsonNode jsonData;
    try {
        jsonData = objectMapper.readTree(data);
    } catch (JsonProcessingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse<>(500, "Error parsing JSON", e.getMessage(),null));
    }

    if (jsonData == null || !jsonData.has("status")) {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body(new ApiErrorResponse<>(417, "Invalid JSON Response", "Missing required fields", null));
    }

    try {
        log.info(jsonData.toString());
        transactionService.updateTransactionByPaymentRequest(paymentRequest, jsonData);
    }catch (Exception e){
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body(new ApiErrorResponse<>(417, "Transaction Update is Not Valid", e.getMessage(), null));

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



//@PostMapping("/recharge-payment")
//public ResponseEntity<?> processRecharge(
//        @RequestParam String mobileNo,
//        @RequestParam String amount,
//        @RequestParam String providerId,
//        @RequestParam String clientId,
//        @RequestParam double cashback,
//        @RequestParam PaymentMethod paymentMethod,
//        @RequestParam Platform platform
//)
//{
//
//    Long userId;
//    try {
//        userId = Long.parseLong(clientId);
//    } catch (NumberFormatException e) {
//        return ResponseEntity.badRequest().body(new ApiErrorResponse<>(400, "Invalid client ID", e.getMessage(), null));
//    }
//
//    if (userService.getUser(userId) == null) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                new ApiErrorResponse<>(404,"User Not found", "Null Pointer Exception",null)
//        );
//    }
//
//    String data = telecomService.rechargePayment(mobileNo,amount,providerId,clientId);
//
//    if (data == null || data.trim().isEmpty()) {
//        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
//                .body(new ApiErrorResponse<>(503, "No Response from Telecom Service", "Empty response received", null));
//    }
//
//    JsonNode jsonData;
//    try {
//        jsonData = objectMapper.readTree(data);
//    } catch (JsonProcessingException e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ApiErrorResponse<>(500, "Error parsing JSON", e.getMessage(),null));
//    }
//
//
//    if (jsonData == null || !jsonData.has("status")) {
//        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
//                .body(new ApiErrorResponse<>(417, "Invalid JSON Response", "Missing required fields", null));
//    }
//
//    String status = jsonData.get("status").asText();
//    String payId;
//    if (!jsonData.has("payid")){
//        payId = "0";
//    }
//    payId = jsonData.get("payid").asText();
//
//    if (payId == null || payId.isEmpty()) {
//        payId = "0";
//    }
//    SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
//    try {
//        subscriptionPlan.setAmount(BigDecimal.valueOf(Double.parseDouble(amount)));
//    } catch (NumberFormatException e) {
//        return ResponseEntity.badRequest().body(new ApiErrorResponse<>(400, "Invalid Amount Format", e.getMessage(), null));
//    }
//
//    Transaction transaction = new Transaction();
//    transaction.setAmount(amount);
//    transaction.setCashBack(String.valueOf(cashback));
//    transaction.setPaymentMethod(paymentMethod);
//    transaction.setUserId(userId);
//    if (status.equals("success")) {
//        transaction.setStatus(TransactionStatus.Success);
//    } else if (status.equals("failure")) {
//        transaction.setStatus(TransactionStatus.Failed);
//    }
//    try {
//        transaction.setAggregatedTransactionId(Long.parseLong(payId));
//    } catch (NumberFormatException e) {
//        return ResponseEntity.badRequest().body(new ApiErrorResponse<>(400, "Invalid Pay ID", e.getMessage(), null));
//    }
//
//    transaction.setPlatformType(platform);
//    transaction.setSubscriptionPlan(subscriptionPlan);
//
//    userService.updateUserCashBackTransactions(userId,cashback);
//    transactionService.addTransaction(transaction);
//
//    ApiResponse<JsonNode> apiResponse = new ApiResponse<>(202,"Recharge Successful", jsonData);
//    return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponse);
//}
