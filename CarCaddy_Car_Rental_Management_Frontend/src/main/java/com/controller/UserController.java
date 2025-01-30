package com.controller;

import java.util.ArrayList;
import java.util.Map;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Car;
import com.model.Customer;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	private final String BASE_URL = "http://localhost:8000"; 
	
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
	  public String filter(Model model) {
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
    public String showBookingsPage(Model model) {
        return "user/bookings"; 

    }
    
    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInUser");
        if (customer == null) {
            return "redirect:/"; // Redirect to home if not logged in
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

    
    
    
    
    
}
