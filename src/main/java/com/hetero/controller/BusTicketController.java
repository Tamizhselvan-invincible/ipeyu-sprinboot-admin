package com.hetero.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hetero.models.bus.City;
import com.hetero.models.bus.BlockTicketRequest;
import com.hetero.models.bus.TripRequest;
import com.hetero.service.CityService;
import com.hetero.service.PSBusTicketBookingService;
import com.hetero.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


@RestController
@RequestMapping("/bus")
@CrossOrigin(origins = {"http://52.66.253.103", "http://localhost:8008"})
public class BusTicketController {

    @Autowired
    private CityService cityService;
    @Autowired
    private PSBusTicketBookingService busTicketBookingService;


    @GetMapping("/cities")
    public ResponseEntity<?> getCitiesFromService() {
        ApiResponse<List<City>> apiResponse = new ApiResponse<>(HttpStatus.ACCEPTED.value(), "Cities Fetched From Database",cityService.getCities());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponse);
    }

    @PostMapping("/cities/save")
    public ResponseEntity<?> saveCities() throws JsonProcessingException {
        ResponseEntity<?> response = busTicketBookingService.getSourceCities();
        if (response.getBody() instanceof Map) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(response.getBody());
            cityService.saveCitiesFromJson(jsonResponse);
        } else if (response.getBody() instanceof String) {
            cityService.saveCitiesFromJson(response.getBody().toString());
        }
        ApiResponse<String> apiResponse = new ApiResponse<>(HttpStatus.ACCEPTED.value(),"Cities saved successfully!", null);
//        return ResponseEntity.status(HttpStatus.ACCEPTED).body(apiResponse);
        return response;
    }

    @PostMapping("/availabletrips")
    public ResponseEntity<?> getAvailableTrips(@RequestBody TripRequest tripRequest) {

        System.out.println(tripRequest.toString());
        return busTicketBookingService.getAvailableTrips(
                tripRequest.getSourceId(), tripRequest.getDestinationId(),
                new SimpleDateFormat("yyyy-MM-dd").format(tripRequest.getDateOfJourney()));

    }

    @PostMapping("/tripdetails")
    public ResponseEntity<?> getTripDetailsFromService(@RequestParam Long tripId) {
        return busTicketBookingService.getCurrentTripDetails(tripId);
    }

    @PostMapping("/boardingDetails")
    public ResponseEntity<?> getBoardingDetailsFromService(@RequestParam Long boardingId, @RequestParam Long tripId) {
        return busTicketBookingService.getBoardingPointDetails(boardingId, tripId);
    }

    @PostMapping("/block-ticket")
    public ResponseEntity<?> blockTicketFromService(@RequestBody BlockTicketRequest request) throws JsonProcessingException {
        return busTicketBookingService.blockTicket(request);
    }

    @PostMapping("/book-ticket")
    public ResponseEntity<?> bookTicketFromService(@RequestParam Long refId, @RequestParam Long amount) {
        return  busTicketBookingService.bookTicket(refId, amount);
    }
    @PostMapping("/check-booked-ticket")
    public ResponseEntity<?> checkBookedTicketFromService(@RequestParam Long refId) {
        return busTicketBookingService.checkBookedTicket(refId);

    }
    @PostMapping("/get-booked-ticket")
    public ResponseEntity<?> getBookedTicketFromService(@RequestParam Long refId) {
        return busTicketBookingService.getBookedTicket(refId);
    }

    @PostMapping("/cancellation-ticket-data")
    public ResponseEntity<?> getCancellationTicketDataFromService(
            @RequestParam Long refId,
            @RequestBody Map<Integer, String> seatsToCancel) {

        return busTicketBookingService.getCancellationTicketData(refId);
    }

    @PostMapping("/cancel-ticket")
    public ResponseEntity<?> cancelTicketFromService(
            @RequestParam Long refId,
            @RequestBody Map<Integer, String> seatsToCancel) {

       return busTicketBookingService.cancelTicket(refId, seatsToCancel);
    }




}

//@RestController
//@RequestMapping("/bus")
//public class BusTicketController {
//
//    @Autowired
//    private CityService cityService;
//
//    @Autowired
//    private BusTicketBookingService busTicketBookingService;
//
//
//
//    @PostMapping("/cities/save")
//    public String saveCities() throws IOException {
//        String jsonResponse = busTicketBookingService.getSourceCities();
//        cityService.saveCitiesFromJson(jsonResponse);
//        return "Cities saved successfully!";
//    }
//
//    @GetMapping("/cities")
//    public ResponseEntity<List<City>> getCities() {
//        return ResponseEntity.ok(cityService.getCities());
//    }
//
//    @PostMapping("/availabletrips") // Define the POST mapping
//    public ResponseEntity<String> getAvailableTripsFromPaySpringAPI(
//            @RequestBody TripRequest tripRequest ) throws IOException, ParseException {
//
//        int sourceId = tripRequest.getSourceId();
//
//        int destinationId = tripRequest.getDestinationId();
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        String formattedDate = dateFormat.format(tripRequest.getDateOfJourney());
//
//        return ResponseEntity.ok(busTicketBookingService.getAvailableTrips(sourceId, destinationId, formattedDate));
//
//    }
//
//
//    @PostMapping("/tripdetails")
//    public ResponseEntity<String> getTripDetails(@RequestParam String tripId) throws IOException {
//
//        Long tripID = Long.parseLong(tripId);
//        return ResponseEntity.ok(busTicketBookingService.getCurrentTripDetails(tripID));
//    }
//
//
//    @PostMapping("/boardingDetails")
//    public ResponseEntity<String> getBoardingDetails(
//            @RequestParam String boardingId,
//            @RequestParam String tripId
//       ) throws IOException {
//
//        Long boardingID = Long.parseLong(boardingId);
//        Long tripID = Long.parseLong(tripId);
//
//        return ResponseEntity.ok(busTicketBookingService.getBoardingPointDetails(boardingID, tripID));
//    }
//
//
//        @PostMapping("/block-ticket")
//        public ResponseEntity<?> blockTicketForUser(@RequestBody BlockTicketRequest request) throws IOException {
//            String response = busTicketBookingService.blockTicket(request);
//            try {
//                return ResponseEntity.ok(new ObjectMapper().readTree(response));
//            } catch (JsonProcessingException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid JSON response from service");
//            }
//        }
//
//
//        @PostMapping("/book-ticket")
//        public ResponseEntity<?> bookTicketForUser(@RequestParam Long refId,
//                                        @RequestParam Long amount) throws IOException {
//        return ResponseEntity.ok(busTicketBookingService.bookTicket(refId, amount));
//        }
//
//
//    @PostMapping("/cancel-ticket")
//    public ResponseEntity<?> cancelTicket(
//            @RequestParam Long refId,
//            @RequestBody Map<Integer, String> seatsToCancel) {
//
//        String response = busTicketBookingService.cancelTicket(refId, seatsToCancel);
////        return handleApiResponse(response);
//    }
//
//
//}
