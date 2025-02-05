package com.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Car;

import com.model.Customer;
import com.model.Employee;
import com.model.Rental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class RentalController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String BASE_URL = "http://localhost:8000/rentals"; // Adjust base URL for your backend service

    // Home Page
    @GetMapping("/rental-management/home")
    public String getHomePage() {
        return "admin/rental-management/home5"; 
    }

    // View All Rentals
    @GetMapping("/rentals/all")
    public String viewAllBookings(Model model) {
        try { 
            ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL + "/viewAllBookings",
                HttpMethod.GET,
                null,
                List.class
            );
            model.addAttribute("rentals", response.getBody());
            return "admin/rental-management/all_rentals5"; 
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Error retrieving rentals: " + e.getMessage());
            return "error"; 
        }
    }

    // Show the booking form
    @GetMapping("/rentals/book")
    public String showBookingForm(Model model) {

        Rental rental = new Rental();
   
        model.addAttribute("rentalRequest", rental);
        return "admin/rental-management/book5"; 
    }

    // Handle car booking
    @PostMapping("/admin/book")
    public String createBooking(@ModelAttribute Rental rentalRequest,BindingResult result, Model model) {
        try {
            Long carId = rentalRequest.getCar().getCarId();
            Long customerId = rentalRequest.getCustomer().getId();

            // Fetch Car rental rate
            ResponseEntity<BigDecimal> rentalRateResponse = restTemplate.exchange(
                 "http://localhost:8000/getRentalRate/" + carId,
                HttpMethod.GET,
                null,
                BigDecimal.class
            );

            if (!rentalRateResponse.getStatusCode().is2xxSuccessful() || rentalRateResponse.getBody() == null) {
                model.addAttribute("error", "Rental rate not found for car ID: " + carId);
                return "admin/rental-management/book5"; // Redirect to an error page
            }
            BigDecimal rentalRate = rentalRateResponse.getBody();

            // Fetch Customer's loyalty points
            ResponseEntity<Integer> loyaltyPointsResponse = restTemplate.exchange(
                 "http://localhost:8000/getLoyaltyPoints/" + customerId,
                HttpMethod.GET,
                null,
                Integer.class
            );
            int loyaltyPoints = (loyaltyPointsResponse.getStatusCode().is2xxSuccessful() && loyaltyPointsResponse.getBody() != null)
                                ? loyaltyPointsResponse.getBody()
                                : 0;

            // Validate rental dates
            LocalDate startDate = rentalRequest.getStartDate();
            LocalDate endDate = rentalRequest.getEndDate();
            if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
                model.addAttribute("error", "Invalid rental dates.");
                return "admin/rental-management/book5";
            }

            // Calculate rental fare
            long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);
            BigDecimal fare = rentalRate.multiply(BigDecimal.valueOf(rentalDays));

            // Determine discount based on loyalty points
            float discountPercentage;
            if (loyaltyPoints <= 5) {
                discountPercentage = 2.0f;
            } else if (loyaltyPoints <= 10) {
                discountPercentage = 5.0f;
            } else if (loyaltyPoints <= 20) {
                discountPercentage = 10.0f;
            } else {
                discountPercentage = 15.0f;
            }

            // Apply discount
            BigDecimal discountAmount = fare.multiply(BigDecimal.valueOf(discountPercentage / 100));
            BigDecimal finalFare = fare.subtract(discountAmount);
            rentalRequest.setFare(finalFare.floatValue());
            rentalRequest.setDiscount(discountAmount.floatValue());
            rentalRequest.setBookingStatus("Pending");

         

            // Save rental booking
           

            // Pass the booking details to the frontend
            model.addAttribute("rentalRequest", rentalRequest);
           

            return "admin/rental-management/bookingConfirmation"; // Redirect to the booking confirmation page

        } catch (Exception e) {
            model.addAttribute("error", "Error processing booking: " + e.getMessage());
            return "admin/rental-management/book5"; // Redirect to the error page
        }
    }
    
    
    @PostMapping("/admin/bookCar")
    public String confirmBooking(@ModelAttribute Rental rentalRequest, BindingResult result, Model model) {
        try {
            Long customerId = rentalRequest.getCustomer().getId();

            // Fetch Customer's loyalty points
            ResponseEntity<Integer> loyaltyPointsResponse = restTemplate.exchange(
                "http://localhost:8000/getLoyaltyPoints/" + customerId,
                HttpMethod.GET,
                null,
                Integer.class
            );

            int loyaltyPoints = (loyaltyPointsResponse.getStatusCode().is2xxSuccessful() && loyaltyPointsResponse.getBody() != null)
                                ? loyaltyPointsResponse.getBody()
                                : 0;

            rentalRequest.setBookingStatus("Completed");

            // Increment and update loyalty points
           

            // Save rental request to backend service
            ResponseEntity<Rental> bookingResponse = restTemplate.postForEntity(
                "http://localhost:8000/rentals/bookCar",
                rentalRequest,
                Rental.class
            );

            if (bookingResponse.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("message", "Booking Successful!");
            } else {
                model.addAttribute("message", "Booking Unsuccessful. Please try again.");
                return "admin/rental-management/bookingStatus";
            }
            
            
            int updatedLoyaltyPoints = loyaltyPoints + 1;
            restTemplate.exchange(
                "http://localhost:8000/updateLoyaltyPoints/" + customerId + "/" + updatedLoyaltyPoints,
                HttpMethod.PUT,
                null,
                Customer.class
            );

            return "admin/rental-management/bookingStatus"; // Redirect to booking status page

        } catch (HttpClientErrorException e) {
        	 Map<String, String> errors=null;
				try {
					errors = new ObjectMapper().readValue(
					    e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
				} catch (JsonMappingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//			
		
			// Map backend errors to BindingResult				
			for(Map.Entry<String, String> entryset : errors.entrySet()) {
				String field = entryset.getKey();
				String errorMsg = entryset.getValue();							
				result.rejectValue(field,"",errorMsg);
			}
            return "admin/rental-management/bookingConfirmation"; // Redirect to booking status page with error message
        }
    }





    // Show the modify booking form
    @GetMapping("/rentals/modify")
    public String showModifyForm(Model model) {
        model.addAttribute("modifyRentalRequest", new Rental());
        return "admin/rental-management/modify5";  
    }

    // Handle booking modification
    @PostMapping("/rentals/modify")
    public String modifyBooking(@ModelAttribute Rental modifyRentalRequest, BindingResult result, Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Rental> request = new HttpEntity<>(modifyRentalRequest, headers);

            restTemplate.exchange(
                BASE_URL + "/updateBooking/" + modifyRentalRequest.getBookingId() , 
                HttpMethod.POST,
                request,
                Void.class
            );
            model.addAttribute("successMessage", "Booking modified successfully!");
            return "modify5";  
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Modification failed: " + e.getMessage());
            return "admin/rental-management/modify5"; 
        }
    }

    // Show the cancel booking form
    @GetMapping("/rentals/cancel")
    public String showCancelForm(Model model) {
        model.addAttribute("bookingId", new String());
        return "admin/rental-management/cancel5"; 
    }

    // Handle booking cancellation
    @PostMapping("/rentals/cancel")
    public String cancelBooking(@RequestParam int bookingId, Model model) {
        try {
            restTemplate.exchange(
                BASE_URL + "/cancelBooking/" + bookingId,
                HttpMethod.POST,
                null,
                Void.class
            );
            model.addAttribute("successMessage", "Booking canceled successfully!");
            return "redirect:/rentals/all"; 
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Cancellation failed: " + e.getMessage());
            return "admin/rental-management/cancel5"; 
        }
    }

    // Show the return form
    @GetMapping("/return")
    public String showReturnForm(Model model) {
        model.addAttribute("bookingId", new String());
        return "admin/rental-management/cancel5";  
    }

    // Handle return acknowledgment
    @PostMapping("/return")
    public String acknowledgeReturn(@RequestParam Long bookingId, Model model) {
        try {
            restTemplate.exchange(
                BASE_URL + "/return?bookingId=" + bookingId,
                HttpMethod.POST,
                null,
                Void.class
            );
            model.addAttribute("successMessage", "Car return acknowledged successfully!");
            return "return5"; 
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Return acknowledgment failed: " + e.getMessage());
            return "return5"; 
        }
    }
    
    
}
