package com.hetero.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hetero.models.telecom.BillPaymentRequest;
import com.hetero.utils.ApiErrorResponse;
import com.hetero.utils.ApiResponse;
import com.hetero.utils.OkHttpClientProvider;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.util.Map;


@Service
public class TelecomScrizaAPIService {

    @Autowired
    private OkHttpClientProvider okHttpClientProvider;

    private static final Logger log = LogManager.getLogger(TelecomScrizaAPIService.class);
    @Value("${application.scriza.base-url}")
    String baseURL;

    @Value("${application.scriza.api-token}")
    String apiKey;

    @Autowired
    PaymentVerificationService paymentVerificationService;

    String environment = "UAT";

    private final ObjectMapper objectMapper = new ObjectMapper();

    // TODO: Need To Modify the redundant Code

    // * Mobile Recharge Plans
    public String rechargePayment(String mobileNo, String amount, String providerId, String clientId, String envi) {
        HttpUrl url = buildUrl("api/telecom/v1/payment", Map.of(
                "api_token", apiKey,
                "number", mobileNo,
                "amount", amount,
                "provider_id", providerId,
                "client_id", clientId,
                "environment", envi
        ));

        String responseString = executeGetRequest(url);

        if (responseString == null) {
            return "{\"error_code\": 500, \"error_message\": \"Failed to process request\"}";
        }


        JSONObject jsonResponse = new JSONObject(responseString);
        JSONObject transactionDetails = jsonResponse.optJSONObject("transaction_details"); // Extract nested JSON

        String payId = transactionDetails != null ? transactionDetails.optString("payid", "") : "";
        String operatorRef = transactionDetails != null ? transactionDetails.optString("operator_ref", "") : "";

        String status = "success".equals(jsonResponse.optString("status")) ? "success" : "failure";

       String callBackURL =  callCallbackURL(payId, operatorRef, mobileNo, providerId, clientId, amount, status);

        return callBackURL;
    }


    // ! This Method Gives Error Check the Documentation
    // ! Need to Change
    /*
     ? Final assessment is revels this is works like a Recharge API.
     ? This Requires the amount Field which is the documentation doesn't mention
     ? I don't know this is the flow or not. currently I'm use the same "/payment"
     ? Endpoint for both API calls.
    */
    private String callCallbackURL(
            String payId,
            String operatorRef,
            String mobileNo,
            String providerId,
            String clientId,
            String amount,
            String status) {
        HttpUrl callbackUrl = buildUrl("api/telecom/v1/payment", Map.of(
                "api_token", apiKey,
                "payid", payId,
                "status", status,
                "operator_ref", operatorRef,
                "client_id", clientId,
                "number", mobileNo,
                "amount", amount,
                "provider_id", providerId,
                "wallet_type", "1",
                "environment", environment
        ));
        String callBackResponse = executeGetRequest(callbackUrl);
        return callBackResponse;
    }

    // Generic Method to Execute API Calls
    private String executeGetRequest(HttpUrl url) {
        OkHttpClient client = okHttpClientProvider.getClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("API Error: {} - {}", response.code(), response.message());
                return null;
            }

            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            log.error("API Error: {} - {}", e.toString(), e.getMessage());
            log.error("Exception in API call: ", e);
            return null;
        }
    }

    // Generic Method to Build URLs
    private HttpUrl buildUrl(String path, Map<String, String> params) {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("https")
                .host(baseURL.replace("https://", "").replace("http://", ""))
                .addPathSegments(path);

        params.forEach(urlBuilder::addQueryParameter);
        return urlBuilder.build();
    }


    public  ResponseEntity<?> getBalanceAmount(){

        String url = baseURL+"/api/telecom/v1/check-balance?api_token="+apiKey;

        OkHttpClient client = okHttpClientProvider.getClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {

            return getResponseEntityFromResponse(response);
        } catch (Exception e) {
            log.error("Exception in API call: ", e);
            ApiErrorResponse<String> errorResponse = new ApiErrorResponse<>(500, "Internal Server Error", e.getMessage(),null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    public  boolean verifyBalanceAmount(Double amount){

        String url = baseURL+"/api/telecom/v1/check-balance?api_token="+apiKey;

        OkHttpClient client = okHttpClientProvider.getClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {

            assert response.body() != null;
            System.out.println(response.body().string());
            try {
                // Create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();

                // Parse JSON string into JsonNode
                JsonNode rootNode = objectMapper.readTree(response.body().string());

                // Extract the normal_balance value
                String normalBalance = rootNode.path("balance").path("normal_balance").asText();

                double accBalance = Double.parseDouble(normalBalance);
                return accBalance >= amount;
            } catch (Exception e) {
                log.error("Exception in API call: {}", e.getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("Exception in API call: ", e);
            return false;
        }
    }




    // * Recharge Plan Services


    public ResponseEntity<?> makePostRequest(String endpoint, Map<String, String> params) {
        OkHttpClient client = okHttpClientProvider.getClient();

        MultipartBody.Builder formBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        formBuilder.addFormDataPart("api_token", apiKey);

        params.forEach(formBuilder::addFormDataPart);

        Request request = new Request.Builder()
                .url(baseURL + endpoint)
                .post(formBuilder.build())
                .addHeader("environment", environment)
                .addHeader("Accept", "application/json")
                .build();

        System.out.println("Before Call : ");
        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();
            String responseBody = response.body() != null ? response.body().string() : "";

            JsonNode jsonData;
           jsonData = objectMapper.readTree(responseBody);

            if (!response.isSuccessful()) {
                log.error("Error: {} - {}", statusCode, response.message());
                ApiResponse<JsonNode> errorResponse = new ApiResponse<>(statusCode, response.message(), jsonData);
                return ResponseEntity.status(statusCode).body(errorResponse);
            }

            ApiResponse<JsonNode> successResponse = new ApiResponse<>(statusCode, response.message(), jsonData);

            return ResponseEntity.status(statusCode).body(successResponse);
        } catch (Exception e) {
            log.error("Exception in API call: ", e);
            ApiErrorResponse<String> errorResponse = new ApiErrorResponse<>(500, "Internal Server Error", e.getMessage(),null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    // TODO: Need Modify this Also

    public ResponseEntity<?> getPlansService1(String providerId, String stateId) {
        return makePostRequest("/api/plan/v1/prepaid-plan", Map.of(
                "provider_id", providerId,
                "state_id", stateId
        ));
    }

    public ResponseEntity<?> getPlansService2(String providerId, String stateId) {
        return makePostRequest("/api/plan/v1/prepaid-plan2", Map.of(
                "provider_id", providerId,
                "state_id", stateId
        ));
    }

    public ResponseEntity<?> getRofferPlan(String providerId, String mobileNo) {
        System.out.println(mobileNo);
        System.out.println("Before Call in Method: ");
        return makePostRequest("/api/plan/v1/r-offer", Map.of(
                "provider_id", providerId,
                "mobile_number", mobileNo
        ));
    }

    public ResponseEntity<?> getDTHPlans(String providerId) {
        return makePostRequest("/api/plan/v1/dth-plans", Map.of(
                "provider_id", providerId
        ));
    }

    // ! This Method Gives Error
    // ! Even Curl also Gives Error
    // ? Might be the Server Side Problem. Need to be Monitor
    public ResponseEntity<?> findOperator(String mobileNo) {
        return makePostRequest("/api/plan/v1/find-operator", Map.of(
                "mobile_number", mobileNo
        ));
    }

    public ResponseEntity<?> getStateList() {
        return makePostRequest("/api/application/v1/state-list", Map.of());
    }

    public ResponseEntity<?> getProvidersList() {
        return makePostRequest("/api/application/v1/get-provider", Map.of());
    }


    // * Bill Payment
    public ResponseEntity<? extends Object> processBillPayment(BillPaymentRequest request) {

        OkHttpClient client = okHttpClientProvider.getClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_token", apiKey)
                .addFormDataPart("provider_id", request.getProviderId())
                .addFormDataPart("optional1", request.getOptional1()==null?"":request.getOptional1())
                .addFormDataPart("optional2", request.getOptional2()==null?"":request.getOptional2())
                .addFormDataPart("optional3", request.getOptional3()== null?"":request.getOptional3())
                .addFormDataPart("optional4", request.getOptional4()==null?"":request.getOptional4())
                .addFormDataPart("amount", request.getAmount())
                .addFormDataPart("client_id", request.getClientId())
                .addFormDataPart("type", "2")
                .addFormDataPart("environment", environment)
                .build();

        Request httpRequest = new Request.Builder()
                .url(baseURL+"/api/telecom/v1/payment")
                .post(requestBody)
                .addHeader("environment", environment)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error: {} - {}", response.code(), response.message());
                ApiErrorResponse<JsonNode> errorResponse = new ApiErrorResponse<>(response.code(), "Request Failure", response.message(), null);
                return ResponseEntity.status(response.code()).body(errorResponse);
            }
            ObjectMapper objectMapper = new ObjectMapper();

            assert response.body() != null;
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String payid = jsonNode.get("payid").asText();

            return verifyBillPayment(request,"919999999999", "cash");

        } catch (Exception e) {
            log.error("Exception in API call: ", e);
            ApiErrorResponse<String> errorResponse = new ApiErrorResponse<>(500, "Internal Server Error", e.getMessage(),null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    public ResponseEntity<? extends Object> verifyBillPayment(BillPaymentRequest request, String customerNumber,String paymentMode) {

        OkHttpClient client = okHttpClientProvider.getClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseURL + "/api/telecom/v1/payment").newBuilder()
                .addQueryParameter("api_token", apiKey)
                .addQueryParameter("provider_id", request.getProviderId())
                .addQueryParameter("number", customerNumber)
                .addQueryParameter("optional1", request.getOptional1() == null ? "" : request.getOptional1())
                .addQueryParameter("optional2", request.getOptional2() == null ? "" : request.getOptional2())
                .addQueryParameter("optional3", request.getOptional3() == null ? "" : request.getOptional3())
                .addQueryParameter("optional4", request.getOptional4() == null ? "" : request.getOptional4())
                .addQueryParameter("amount", request.getAmount())
                .addQueryParameter("client_id", request.getClientId())
                .addQueryParameter("payment_mode", paymentMode)
                .addQueryParameter("type", "2")
                .addQueryParameter("environment", environment);


        String finalUrl = urlBuilder.build().toString();  // Construct the final URL

        // Build the GET request
        Request httpRequest = new Request.Builder()
                .url(finalUrl)
                .get()  // GET request
                .addHeader("environment", environment)  // Add required headers
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            int statusCode = response.code();
            String responseBody = response.body() != null ? response.body().string() : "";

            JsonNode jsonData;
            jsonData = objectMapper.readTree(responseBody);

            if (!response.isSuccessful()) {
                log.error("Error: {} - {}", statusCode, response.message());
                ApiErrorResponse<JsonNode> errorResponse = new ApiErrorResponse<>(response.code(), "Request Failure", response.message(), null);
                return ResponseEntity.status(statusCode).body(errorResponse);
            }

            ApiResponse<JsonNode> successResponse = new ApiResponse<>(statusCode, "Success", jsonData);

            return ResponseEntity.status(statusCode).body(successResponse);
        } catch (Exception e) {
            log.error("Exception in API call: ", e);
            ApiErrorResponse<String> errorResponse = new ApiErrorResponse<>(500, "Internal Server Error", e.getMessage(),null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    public ResponseEntity<?> getResponseEntityFromResponse(Response response) {

        try{
            int statusCode = response.code();
            String responseBody = response.body() != null ? response.body().string() : "";

            JsonNode jsonData;
            jsonData = objectMapper.readTree(responseBody);

            if (!response.isSuccessful()) {
                log.error("Error: {} - {}", statusCode, response.message());
                ApiResponse<JsonNode> errorResponse = new ApiResponse<>(statusCode, response.message(), jsonData);
                return ResponseEntity.status(statusCode).body(errorResponse);
            }

            ApiResponse<JsonNode> successResponse = new ApiResponse<>(statusCode, "Success", jsonData);

            return ResponseEntity.status(statusCode).body(successResponse);
        } catch (Exception e) {
            log.error("Exception in API call: ", e);
            ApiErrorResponse<String> errorResponse = new ApiErrorResponse<>(500, "Internal Server Error", e.getMessage(),null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}





//    public String rechargePayment(String mobileNo, String amount, String providerId, String clientId) {
//
//        HttpUrl url = new HttpUrl.Builder()
//                .scheme("https") // Use https or http
//                .host(baseURL.replace("https://", "").replace("http://", ""))
//                .addPathSegments("api/telecom/v1/payment")
//                .addQueryParameter("api_token", apiKey)
//                .addQueryParameter("number", mobileNo)
//                .addQueryParameter("amount", amount)
//                .addQueryParameter("provider_id", providerId)
//                .addQueryParameter("client_id", clientId)
//                .addQueryParameter("environment", environment)
//                .build();
//
//        OkHttpClient client = okHttpClientProvider.getClient();
//
//        // Build GET request
//        Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .addHeader("Accept", "application/json") // Optional header
//                .build();
//
//        // Execute the request
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                log.error("Error: {} - {}", response.code(), response.message());
//                return String.format("{\"error_code\": %d, \"error_message\": \"%s\"}", response.code(), response.message());
//            }
//            // Parse the JSON response
//
//            System.out.println("Before Call Back URL");
//            JSONObject jsonResponse = new JSONObject(response.body().string());
//            System.out.println(jsonResponse.toString());
//            if ("success".equals(jsonResponse.optString("status"))) {
//                System.out.println("At Call Back URL");
//                String payId = jsonResponse.optString("payid");
//                String operatorRef = jsonResponse.optString("operator_ref");
//                callCallbackURL(payId, operatorRef, mobileNo, providerId, clientId,amount);
//            }
//
//            return response.body().string();
//
//        } catch (Exception e) {
//            log.error("Exception in API call: ", e);
//            return String.format("{\"error_code\": 500, \"error_message\": \"%s\"}", e.getMessage());
//        }
//    }
//
//



//public String getPlansService1(String providerId,String stateId) {
//
//
//    OkHttpClient client = okHttpClientProvider.getClient();
//
//    RequestBody formBody = new MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("api_token", apiKey)
//            .addFormDataPart("provider_id", providerId)
//            .addFormDataPart("state_id", stateId)
//            .build();
//
//    Request request = new Request.Builder()
//            .url(baseURL+"/api/plan/v1/prepaid-plan")
//            .post(formBody)
//            .addHeader("environment", environment)
//            .addHeader("Accept", "application/json") // Optional header
//            .build();
//
//    try (Response response = client.newCall(request).execute()) {
//        if (!response.isSuccessful()) {
//            log.error("Error: {} - {}", response.code(), response.message());
//            return String.format("{\"error_code\": %d, \"error_message\": \"%s\"}", response.code(), response.message());
//        }
//        return response.body().string();
//    } catch (Exception e) {
//        log.error("Exception in API call: ", e);
//        return String.format("{\"error_code\": 500, \"error_message\": \"%s\"}", e.getMessage());
//    }
//}
//
//public String getPlansService2(String providerId,String stateId) {
//
//    OkHttpClient client = okHttpClientProvider.getClient();
//
//    RequestBody formBody = new MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("api_token", apiKey)
//            .addFormDataPart("provider_id", providerId)
//            .addFormDataPart("state_id", stateId)
//            .build();
//
//    Request request = new Request.Builder()
//            .url(baseURL+"/api/plan/v1/prepaid-plan2")
//            .post(formBody)
//            .addHeader("environment", environment)
//            .addHeader("Accept", "application/json") // Optional header
//            .build();
//
//    try (Response response = client.newCall(request).execute()) {
//        if (!response.isSuccessful()) {
//            log.error("Error: {} - {}", response.code(), response.message());
//            return String.format("{\"error_code\": %d, \"error_message\": \"%s\"}", response.code(), response.message());
//        }
//        return response.body().string();
//    } catch (Exception e) {
//        log.error("Exception in API call: ", e);
//        return String.format("{\"error_code\": 500, \"error_message\": \"%s\"}", e.getMessage());
//    }
//}
//
//
//
//    // ! This Method Gives Error Check the Documentation
//    private void callCallbackURL(String payId, String operatorRef, String mobileNo, String providerId, String clientId,String amount) {
//        HttpUrl callbackUrl = new HttpUrl.Builder()
//                .scheme("https")
//                .host(baseURL.replace("https://", "").replace("http://", ""))
//                .addPathSegments("api/telecom/v1/payment")// Update this to the actual endpoint path
//                .addQueryParameter("api_token", apiKey)
//                .addQueryParameter("payid", payId)
//                .addQueryParameter("status", "success")
//                .addQueryParameter("operator_ref", operatorRef)
//                .addQueryParameter("client_id", clientId)
//                .addQueryParameter("number", mobileNo)
//                .addQueryParameter("amount", amount)
//                .addQueryParameter("provider_id", providerId)
//                .addQueryParameter("wallet_type", "1")// Assuming it's always 1
//                .addQueryParameter("environment", environment)
//                .build();
//
//        OkHttpClient client = okHttpClientProvider.getClient();
//
//        Request callbackRequest = new Request.Builder()
//                .url(callbackUrl)
//                .get()
//                .addHeader("Accept", "application/json")
//                .build();
//
//        try (Response response = client.newCall(callbackRequest).execute()) {
//            if (!response.isSuccessful()) {
//                log.error("Callback URL Error: {} - {}", response.code(), response.message());
//            }
//            System.out.println( response.body().string());
//        } catch (Exception e) {
//            log.error("Exception in Callback API call: ", e);
//        }
//    }





//public String getRofferPlan(String providerId, String mobileNo) {
//    OkHttpClient client = okHttpClientProvider.getClient();
//
//    RequestBody requestBody = new MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("api_token", apiKey)
//            .addFormDataPart("provider_id", providerId)
//            .addFormDataPart("mobile_number", mobileNo)
//            .build();
//
//    // Creating the request
//    Request request = new Request.Builder()
//            .url(baseURL+"/api/plan/v1/r-offer")
//            .post(requestBody)
//            .addHeader("environment", environment)
//            .build();
//
//
//    try (Response response = client.newCall(request).execute()) {
//        if (!response.isSuccessful()) {
//            log.error("Error: {} - {}", response.code(), response.message());
//            log.error("Response Body: {}", response.body().string());
//            return String.format("{\"error_code\": %d, \"error_message\": \"%s\"}", response.code(), response.message());
//        }
//        return response.body().string();
//    } catch (Exception e) {
//        log.error("Exception in API call: ", e);
//        return String.format("{\"error_code\": 500, \"error_message\": \"%s\"}", e.getMessage());
//    }
//}
//
//public String getDTHPlans(String providerId) {
//    OkHttpClient client = okHttpClientProvider.getClient();
//
//    RequestBody requestBody = new MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("api_token", apiKey)
//            .addFormDataPart("provider_id", providerId)
//            .build();
//
//    Request request = new Request.Builder()
//            .url(baseURL+"/api/plan/v1/dth-plans")
//            .post(requestBody)
//            .addHeader("environment", environment)
//            .build();
//
//
//    try (Response response = client.newCall(request).execute()) {
//        if (!response.isSuccessful()) {
//            log.error("Error: {} - {}", response.code(), response.message());
//            log.error("Response Body: {}", response.body().string());
//            return String.format("{\"error_code\": %d, \"error_message\": \"%s\"}", response.code(), response.message());
//        }
//        return response.body().string();
//    } catch (Exception e) {
//        log.error("Exception in API call: ", e);
//        return String.format("{\"error_code\": 500, \"error_message\": \"%s\"}", e.getMessage());
//    }
//}
//
//
//// ! This Method Gives Error
//// ! Even Curl also Gives Error
//// ? Might be the Server Side Problem. Need to be Monitor

//public String findOperator(String mobileNo) {
//    OkHttpClient client = okHttpClientProvider.getClient();
//
//    RequestBody requestBody = new MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("api_token", apiKey)
//            .addFormDataPart("mobile_number", mobileNo)
//            .build();
//
//    Request request = new Request.Builder()
//            .url(baseURL+"/api/plan/v1/find-operator")
//            .post(requestBody)
//            .addHeader("environment", environment)
//            .build();
//
//
//    try (Response response = client.newCall(request).execute()) {
//        if (!response.isSuccessful()) {
//            log.error("Error: {} - {}", response.code(), response.message());
//            log.error("Response Body: {}", response.body().string());
//            return String.format("{\"error_code\": %d, \"error_message\": \"%s\"}", response.code(), response.message());
//        }
//        return response.body().string();
//    } catch (Exception e) {
//        log.error("Exception in API call: ", e);
//        return String.format("{\"error_code\": 500, \"error_message\": \"%s\"}", e.getMessage());
//    }
//}
//
//public String getStateList() {
//    OkHttpClient client = okHttpClientProvider.getClient();
//
//    RequestBody requestBody = new MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("api_token", apiKey)
//            .build();
//
//    Request request = new Request.Builder()
//            .url(baseURL+"/api/application/v1/state-list")
//            .post(requestBody)
//            .addHeader("environment", environment)
//            .build();
//
//
//    try (Response response = client.newCall(request).execute()) {
//        if (!response.isSuccessful()) {
//            log.error("Error: {} - {}", response.code(), response.message());
//            log.error("Response Body: {}", response.body().string());
//            return String.format("{\"error_code\": %d, \"error_message\": \"%s\"}", response.code(), response.message());
//        }
//        return response.body().string();
//    } catch (Exception e) {
//        log.error("Exception in API call: ", e);
//        return String.format("{\"error_code\": 500, \"error_message\": \"%s\"}", e.getMessage());
//    }
//}
//
//// * Bill Payment Services
//
//public String getProvidersList() {
//    OkHttpClient client = okHttpClientProvider.getClient();
//
//    RequestBody requestBody = new MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart("api_token", apiKey)
//            .build();
//
//    Request request = new Request.Builder()
//            .url(baseURL+"/api/application/v1/get-provider")
//            .post(requestBody)
//            .addHeader("environment", environment)
//            .build();
//
//
//    try (Response response = client.newCall(request).execute()) {
//        if (!response.isSuccessful()) {
//            log.error("Error: {} - {}", response.code(), response.message());
//            log.error("Response Body: {}", response.body().string());
//            return String.format("{\"error_code\": %d, \"error_message\": \"%s\"}", response.code(), response.message());
//        }
//        return response.body().string();
//    } catch (Exception e) {
//        log.error("Exception in API call: ", e);
//        return String.format("{\"error_code\": 500, \"error_message\": \"%s\"}", e.getMessage());
//    }
//}
