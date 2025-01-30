package com.controller;

import com.model.Rental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
public class RentalController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String BASE_URL = "http://localhost:8080/api/rentals"; // Adjust base URL for your backend service

    // Home Page
    @GetMapping("////")
    public String getHomePage() {
        return "home5"; 
    }

    // View All Rentals
    @GetMapping("//all")
    public String viewAllBookings(Model model) {
        try {
            ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL + "/all",
                HttpMethod.GET,
                null,
                List.class
            );
            model.addAttribute("rentals", response.getBody());
            return "all_rentals5"; 
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Error retrieving rentals: " + e.getMessage());
            return "error"; 
        }
    }

    // Show the booking form
    @GetMapping("/book")
    public String showBookingForm(Model model) {
        model.addAttribute("rentalRequest", new Rental());
        return "book5"; // Render 'book.html'
    }

    // Handle car booking
    @PostMapping("/book")
    public String bookCar(@ModelAttribute Rental rentalRequest, BindingResult result, Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Rental> request = new HttpEntity<>(rentalRequest, headers);

            ResponseEntity<Integer> response = restTemplate.exchange(
                BASE_URL + "/book", 
                HttpMethod.POST,
                request,
                Integer.class
            );
            model.addAttribute("successMessage", "Booking successful! Your Booking ID: " + response.getBody());
            return "book5"; 
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Booking failed: " + e.getMessage());
            return "book5"; 
        }
    }

    // Show the modify booking form
    @GetMapping("/modify")
    public String showModifyForm(Model model) {
        model.addAttribute("modifyRentalRequest", new Rental());
        return "modify5";  
    }

    // Handle booking modification
    @PostMapping("/modify")
    public String modifyBooking(@ModelAttribute Rental modifyRentalRequest, BindingResult result, Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Rental> request = new HttpEntity<>(modifyRentalRequest, headers);

            restTemplate.exchange(
                BASE_URL + "/modify", 
                HttpMethod.POST,
                request,
                Void.class
            );
            model.addAttribute("successMessage", "Booking modified successfully!");
            return "modify5";  
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Modification failed: " + e.getMessage());
            return "modify5"; 
        }
    }

    // Show the cancel booking form
    @GetMapping("/cancel")
    public String showCancelForm(Model model) {
        model.addAttribute("bookingId", new String());
        return "cancel5"; 
    }

    // Handle booking cancellation
    @PostMapping("/cancel")
    public String cancelBooking(@RequestParam int bookingId, Model model) {
        try {
            restTemplate.exchange(
                BASE_URL + "/cancel?bookingId=" + bookingId,
                HttpMethod.POST,
                null,
                Void.class
            );
            model.addAttribute("successMessage", "Booking canceled successfully!");
            return "cancel5"; 
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Cancellation failed: " + e.getMessage());
            return "cancel5"; 
        }
    }

    // Show the return form
    @GetMapping("/return")
    public String showReturnForm(Model model) {
        model.addAttribute("bookingId", new String());
        return "return5";  
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
