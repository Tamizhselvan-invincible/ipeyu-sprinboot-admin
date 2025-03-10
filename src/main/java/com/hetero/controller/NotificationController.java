package com.hetero.controller;

import com.hetero.models.PushNotificationRequest;
import com.hetero.models.PushNotificationResponse;
import com.hetero.service.FCMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ExecutionException;


@RestController
@CrossOrigin(origins = "http://52.66.253.103")
public class NotificationController {

    @Autowired
    private FCMService fcmService;

    @PostMapping("/notification")
    public ResponseEntity<?> sendNotification(@RequestBody PushNotificationRequest request) throws ExecutionException, InterruptedException {

        fcmService.sendMessageToToken(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }


}