package com.controller;

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
import java.util.Random;

@Controller
public class RentalController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String BASE_URL = "http://localhost:8000/rentals"; // Adjust base URL for your backend service

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
    public String showBookingForm(@RequestParam("carId") Long carId, 
                                  @RequestParam("customerId") Long customerId, 
                                  Model model) {

        Rental rental = new Rental();
        Car car = new Car();
        car.setCarId(carId);
        Customer customer = new Customer();
        customer.setId(customerId);
        rental.setCar(car);
        rental.setCustomer(customer);


        String employeeApiUrl = "http://localhost:8000/api/employees/employees";


        ResponseEntity<Employee[]> response = restTemplate.getForEntity(employeeApiUrl, Employee[].class);
        Employee[] employees = response.getBody();

        if (employees != null && employees.length > 0) {
            Random random = new Random();
            Employee randomEmployee = employees[random.nextInt(employees.length)];
            rental.setEmployee(randomEmployee);
        } else {
            System.out.println("No employees available");
        }

        model.addAttribute("rentalRequest", rental);
        return "user/book5"; 
    }

    // Handle car booking
    @PostMapping("/book")
    public String createBooking(@ModelAttribute Rental rentalRequest,BindingResult result, Model model) {
        try {
            Long carId = rentalRequest.getCar().getCarId();
            Long customerId = rentalRequest.getCustomer().getId();

            // Fetch Car rental rate
            ResponseEntity<BigDecimal> rentalRateResponse = restTemplate.exchange(
                BASE_URL + "/getRentalRate/" + carId,
                HttpMethod.GET,
                null,
                BigDecimal.class
            );

            if (!rentalRateResponse.getStatusCode().is2xxSuccessful() || rentalRateResponse.getBody() == null) {
                model.addAttribute("error", "Rental rate not found for car ID: " + carId);
                return "user/book5"; // Redirect to an error page
            }
            BigDecimal rentalRate = rentalRateResponse.getBody();

            // Fetch Customer's loyalty points
            ResponseEntity<Integer> loyaltyPointsResponse = restTemplate.exchange(
                BASE_URL + "/getLoyaltyPoints/" + customerId,
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
                return "user/book5";
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

            // Increment and update loyalty points
            int updatedLoyaltyPoints = loyaltyPoints + 1;
            restTemplate.exchange(
                BASE_URL + "/updateLoyaltyPoints/" + customerId + "/" + updatedLoyaltyPoints,
                HttpMethod.PUT,
                null,
                Customer.class
            );

            // Save rental booking
           

            // Pass the booking details to the frontend
            model.addAttribute("rentalRequest", rentalRequest);
           

            return "user/bookingConfirmation"; // Redirect to the booking confirmation page

        } catch (Exception e) {
            model.addAttribute("error", "Error processing booking: " + e.getMessage());
            return "user/book5"; // Redirect to the error page
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
