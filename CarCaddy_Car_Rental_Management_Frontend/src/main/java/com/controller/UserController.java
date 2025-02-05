package com.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Car;
import com.model.Customer;
import com.model.Employee;
import com.model.Rental;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	private final String BASE_URL = "http://localhost:8000"; 
	
	@Autowired
	RestTemplate restTemplate;
	
	Customer  customer;
	
	@GetMapping("/")
    public String showHomePage() {
        return "user/entry"; 
    }
	
	@GetMapping("/user/login")
    public String showLogInPage() {
        return "user/login"; 
    }


	@GetMapping("/user/home")
	public String userHome(HttpSession session, Model model) {
	    Customer loggedInUser = (Customer) session.getAttribute("loggedInUser");
	    if (loggedInUser != null) {
	        model.addAttribute("user", loggedInUser);
	        return "user/home";
	    }
	    return "redirect:/user/login"; // Redirect to login if session is not set
	}
	

    @GetMapping("/user/filter")
	  public String filter(Model model, HttpSession session) {
    	Customer loggedInUser = (Customer) session.getAttribute("loggedInUser");
    	if(loggedInUser == null) {
    		return "redirect:/user/login";
    	}
    	
		  model.addAttribute("cars", new ArrayList<Car>());
		  return "user/filter";
	  }
    
    @GetMapping("/about")
    public String showAboutPage(Model model) {
        return "user/about"; 
    }

    @GetMapping("/contact")
    public String showContactPage(Model model) {
        return "user/contact"; 

    }
    
    @GetMapping("/bookings")
    public String showBookingsPage(Model model, HttpSession session) {
        Customer loggedInUser = (Customer) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/user/login";
        }

        try {
            // Retrieve updated customer details from backend
            ResponseEntity<Customer> response = restTemplate.getForEntity(
            		"http://localhost:8000/getCustomerById/" + loggedInUser.getId(), Customer.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Update session with the latest customer details
                session.setAttribute("loggedInUser", response.getBody());
                model.addAttribute("loggedInUser", response.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();  // Log error if backend request fails
            return "redirect:/user/login";  // Redirect to error page if needed
        }

        return "user/bookings";
    }

    
    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInUser");
        if (customer == null) {
            return "redirect:/user/login"; // Redirect to home if not logged in
        }
        model.addAttribute("customer", customer);
        return "user/profile"; // Thymeleaf template name
    }
    
 

    @GetMapping("/addCustomerForm")
    public String showAddCustomerForm(Model model) {
        model.addAttribute("customer", new Customer()); // Ensure correct attribute name
        return "user/customer-form"; // Corrected to match your form template
    }

    @PostMapping("/addCustomer")
    public String handleAddCustomer(@ModelAttribute Customer customer, BindingResult result, Model model, HttpSession session) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            // Send the customer data to the backend to create a new customer
            ResponseEntity<Customer> response = restTemplate.postForEntity(
                    BASE_URL + "/addCustomer", customer, Customer.class);

            // If the response is successful, store the customer in the session
            if (response.getStatusCode().is2xxSuccessful()) {
                Customer loggedInUser = response.getBody();
                session.setAttribute("loggedInUser", loggedInUser);
            }
        } catch (HttpClientErrorException e) {
            // If there is a validation error from the backend, parse the error response
            Map<String, String> errors = null;
            try {
                // Parse the error response from the backend
                errors = new ObjectMapper().readValue(
                        e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }

            // Add errors to the BindingResult for display on the frontend
            if (errors != null) {
                for (Map.Entry<String, String> entry : errors.entrySet()) {
                    result.rejectValue(entry.getKey(), "", entry.getValue());
                }
            }
            return "user/customer-form"; // Return to the form view with validation errors
        }

        return "redirect:/user/home"; // Redirect to the user's home page after success
    }




  



    @GetMapping("/customers/edit")
    public String showEditCustomerForm(HttpSession session, Model model) {
        // Retrieve the logged-in user from the session
        Customer loggedInUser = (Customer) session.getAttribute("loggedInUser");
        
        if (loggedInUser != null) {
            // Add the logged-in user's data to the model to pre-fill the form
            model.addAttribute("customer", loggedInUser);
            return "user/customer-update"; // Render the edit form
        }
        
        // Redirect to login if no user is in the session
        return "redirect:/user/login";
    }

    @PostMapping("/customers/edit")
    public String updateCustomer(@ModelAttribute Customer customer, BindingResult result, Model model, HttpSession session) {
    	Customer loggedInUser = (Customer) session.getAttribute("loggedInUser");
    	if(loggedInUser == null) {
    		return "redirect:/user/login";
    	}
        RestTemplate restTemplate = new RestTemplate();
        try {
            // Use PUT request to update the customer
        	System.out.println(customer);
            String url = BASE_URL + "/updateCustomer";
            ResponseEntity<Customer> response = restTemplate.exchange(
                    url, HttpMethod.PUT, new HttpEntity<>(customer), Customer.class);

            // If the response is successful, update the session
            if (response.getStatusCode().is2xxSuccessful()) {
                session.setAttribute("loggedInUser", response.getBody());
                model.addAttribute("message", "Customer updated successfully");
                return "redirect:/profile"; // Redirect to profile page
            }
        } catch (HttpClientErrorException e) {
            // Handle errors from the backend
            Map<String, String> errors = parseErrorResponse(e);
            errors.forEach((field, message) -> result.rejectValue(field, "", message));
        }

        // Return to the form if there are errors
        return "user/customer-update";
    }





    // Helper method to parse error response from the backend
    private Map<String, String> parseErrorResponse(HttpClientErrorException e) {
        Map<String, String> errors = null;
        try {
            errors = new ObjectMapper().readValue(
                    e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
        }
        return errors;
    }
    
    
    @PostMapping("/user/login")
    public String handleLogin(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            // Construct the backend login endpoint URL with query parameters
            String loginUrl = BASE_URL + "/auth/user/login?email=" + email + "&password=" + password;

            // Send a POST request to the backend and expect a response of type Customer
            ResponseEntity<Customer> response = restTemplate.postForEntity(loginUrl, null, Customer.class);

            // Check if login is successful
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Customer loggedInUser = response.getBody();
                System.out.println(loggedInUser);

                // Store user information in session
                session.setAttribute("loggedInUser", loggedInUser);

                return "redirect:/user/home"; // Redirect to user home page
            } else {
                model.addAttribute("errorMessage", "Unexpected error occurred. Please try again.");
                return "user/login";
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            // Handle Unauthorized (401) response
            model.addAttribute("errorMessage", "Invalid email or password.");
            return "user/login";
        } catch (HttpClientErrorException.BadRequest e) {
            // Handle Bad Request (400) response
            model.addAttribute("errorMessage", "Bad request. Please check your input.");
            return "user/login";
        } catch (Exception e) {
            // Handle any other exceptions
            model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "user/login";
        }
    }
    
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalidate session
        return "redirect:/";  // Redirect to home page
    }
    
    @GetMapping("/user/getCar")
    public String getCar(@ModelAttribute Car car, Model model, HttpSession session) {
    	Customer loggedInUser = (Customer) session.getAttribute("loggedInUser");
    	if(loggedInUser == null) {
    		return "redirect:/user/login";
    	}
    	
        String registrationNumber = car.getRegistrationNumber(); // Extract registration number
        String backendUrl = "http://localhost:8000/getCar/registrationnumber/" + registrationNumber;

        try {
            ResponseEntity<Car> response = restTemplate.exchange(
                backendUrl,
                HttpMethod.GET,
                null,
                Car.class
            );


            Car fetchedCar = response.getBody();
            if (fetchedCar != null) {
                model.addAttribute("car", fetchedCar); // Add car details to the model
                model.addAttribute("customer", loggedInUser);
                return "user/car"; // Render car.html template
            }
        } catch (HttpClientErrorException.NotFound e) {
            // Backend returns 404 when the car is not found
            model.addAttribute("errorMessage", "Enter a valid registration number");
        } catch (Exception e) {
            // Handle unexpected errors
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        }

        return "user/filter"; // Stay on the search page with the error message

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
                 "http://localhost:8000/getRentalRate/" + carId,
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

         

            // Save rental booking
           

            // Pass the booking details to the frontend
            model.addAttribute("rentalRequest", rentalRequest);
           

            return "user/bookingConfirmation"; // Redirect to the booking confirmation page

        } catch (Exception e) {
            model.addAttribute("error", "Error processing booking: " + e.getMessage());
            return "user/book5"; // Redirect to the error page
        }
    }
    
    
    @PostMapping("/bookCar")
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
                return "user/bookingStatus"; 
            }
            
         // Increment and update loyalty points
            int updatedLoyaltyPoints = loyaltyPoints + 1;
            restTemplate.exchange(
                "http://localhost:8000/updateLoyaltyPoints/" + customerId + "/" + updatedLoyaltyPoints,
                HttpMethod.PUT,
                null,
                Customer.class
            );


           

            return "user/bookingStatus"; // Redirect to booking status page

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
            return "user/bookingConfirmation"; // Redirect to booking status page with error message
        }
    }

    
    
    
    
    
}
