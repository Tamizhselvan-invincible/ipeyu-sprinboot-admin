package com.hetero.controller;


import com.hetero.models.Settings;
import com.hetero.service.SettingsService;
import com.hetero.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@CrossOrigin(origins = "http://52.66.253.103")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;


    @GetMapping
    public ResponseEntity<?> getSettingsDetail(){
        Settings settings = settingsService.getSettings();
        ApiResponse<Settings> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), settings);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PutMapping
    public ResponseEntity<?> updateSettingsDetail(@RequestBody Settings settings){
        Settings updatedSettings = settingsService.updateSettingDetails(settings);

        ApiResponse<Settings> apiResponse = new ApiResponse<>(
                HttpStatus.ACCEPTED.value(),
                HttpStatus.ACCEPTED.getReasonPhrase(),
                updatedSettings);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponse);
    }

    @PostMapping
    public ResponseEntity<?> addSettingsDetail(@RequestBody Settings settings){
        Settings addedSettings = settingsService.registerSettings(settings);

        ApiResponse<Settings> apiResponse = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                HttpStatus.CREATED.getReasonPhrase(),
                addedSettings);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

}
