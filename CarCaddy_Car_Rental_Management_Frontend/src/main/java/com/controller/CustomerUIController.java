package com.controller;

import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import com.model.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

@Controller
public class CustomerUIController {
	
	 private final String BASE_URL = "http://localhost:8000";
	
	 @GetMapping("/customer-management/home")
	    public String home() {
	        return "admin/customer-management/customers"; // returns the view name to be rendered - customers.html
	    }
	 



	    @PostMapping("admin/addCustomer")
	    public String handleAddCustomer(@ModelAttribute Customer customer, BindingResult result, Model model) {
	        RestTemplate restTemplate = new RestTemplate();
	        try {
	            // POST request to backend API
	            ResponseEntity<String> response = restTemplate.postForEntity(
	                BASE_URL + "/addCustomer",
	                customer,
	                String.class
	            );
	            model.addAttribute("message", response.getBody());
	        } catch (HttpClientErrorException e) {
	            // Parse and handle validation errors from backend
	            Map<String, String> errors = null;
	            try {
	                errors = new ObjectMapper().readValue(
	                    e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
	            } catch (JsonMappingException e1) {
	                e1.printStackTrace();
	            } catch (JsonProcessingException e1) {
	                e1.printStackTrace();
	            }
	            // Map backend errors to BindingResult
	            for (Map.Entry<String, String> entry : errors.entrySet()) {
	                String field = entry.getKey();
	                String errorMsg = entry.getValue();
	                result.rejectValue(field, "", errorMsg);
	            }

	            return "admin/customer-management/customer-form"; // Return to the form view with validation errors
	        }
	        return "redirect:/customer-management/home"; // Redirect to the customer list page after successful addition
	    }

	    // New mapping for '/customers/new' route
	    @GetMapping("/customers/new")
	    public String showCreateCustomerForm(Model model) {
	        model.addAttribute("customer", new Customer());
	        return "admin/customer-management/customer-form"; // Form for creating a new customer
	    }

	    // New mapping for '/customers/list' route
	    @GetMapping("/customers/list")
	    public String showCustomerList(Model model) {
	        List<Customer> customers = fetchAllCustomers();
	        model.addAttribute("customers", customers);
	        return "admin/customer-management/customer-list";
	    }

	    // Fetch all customers
	    private List<Customer> fetchAllCustomers() {
	        RestTemplate restTemplate = new RestTemplate();
	        try {
	            ResponseEntity<Customer[]> response = restTemplate.exchange(
	                    BASE_URL + "/getAllCustomers", HttpMethod.GET, null, Customer[].class
	            );
	            if (response.getStatusCode() == HttpStatus.OK) {
	                return Arrays.asList(response.getBody());
	            } else {
	                return Collections.emptyList();
	            }
	        } catch (Exception e) {
	            System.err.println("Error fetching customers:" + e.getMessage());
	            return Collections.emptyList();
	        }
	    }

	    // Search Customer by ID
	    @GetMapping("/customers/search")
	    public String searchCustomerById(@RequestParam("id") Long id, Model model) {
	        RestTemplate restTemplate = new RestTemplate();
	        try {
	            ResponseEntity<Customer> response = restTemplate.getForEntity(
	                    BASE_URL + "/getCustomerById/" + id, Customer.class);
	            model.addAttribute("customer", response.getBody());
	        } catch (HttpClientErrorException e) {
	            // Handle error if customer is not found
	            Map<String, String> errors = parseErrorResponse(e);
	            model.addAttribute("errorMessage", errors != null ? errors.get("message") : "Customer not found.");
	        }
	        return "admin/customer-management/customer-form";  // Return customer form for editing or viewing the customer
	    }

	    // Edit Customer Form (fetch by ID)
	    @GetMapping("/customers/edit/{id}")
	    public String showEditCustomerForm(@PathVariable("id") Long id, Model model) {
	        RestTemplate restTemplate = new RestTemplate();
	        try {
	            ResponseEntity<Customer> response = restTemplate.getForEntity(
	                    BASE_URL + "/getCustomerById/" + id, Customer.class);
	            model.addAttribute("customer", response.getBody());
	        } catch (HttpClientErrorException e) {
	            // Handle error if customer is not found
	            Map<String, String> errors = parseErrorResponse(e);
	            model.addAttribute("errorMessage", errors != null ? errors.get("message") : "Error fetching customer details");
	        }
	        return "admin/customer-management/customer-form";  // Return to the customer form for editing
	    }

	    // Update Customer (submit form)
	    @PostMapping("/updateCustomer/{id}")
	    public String updateCustomer(@PathVariable("id") Long id,
	                                 @Valid @ModelAttribute("customer") Customer customer,
	                                 BindingResult result, Model model) {
	        if (result.hasErrors()) {
	            return "customer-form";  // If validation errors, return to the form
	        }

	        // Update customer via PUT API call
	        RestTemplate restTemplate = new RestTemplate();
	        try {
	            restTemplate.put(BASE_URL + "/updateCustomer/" + id, customer);
	            model.addAttribute("message", "Customer updated successfully");
	        } catch (HttpClientErrorException e) {
	            Map<String, String> errors = parseErrorResponse(e);
	            model.addAttribute("errorMessage", errors != null ? errors.get("message") : "Error updating customer");
	        }
	        return "redirect:/customers/list"; // Redirect to the customer list page after update
	    }

	    // Delete Customer
	    @GetMapping("/customers/delete/{id}")
	    public String deleteCustomer(@PathVariable("id") Long id, Model model) {
	        RestTemplate restTemplate = new RestTemplate();
	        try {
	            restTemplate.delete(BASE_URL + "/deleteCustomer/" + id);
	            model.addAttribute("message", "Customer deleted successfully");
	        } catch (HttpClientErrorException e) {
	            Map<String, String> errors = parseErrorResponse(e);
	            model.addAttribute("errorMessage", errors != null ? errors.get("message") : "Error deleting customer");
	        }
	        return "redirect:/customers/list"; // Redirect to the customer list page after deletion
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


}
