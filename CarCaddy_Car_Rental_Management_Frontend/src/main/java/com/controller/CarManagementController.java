package com.controller;


import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.model.Car;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class CarManagementController {
	
	@Autowired
	private RestTemplate restTemplate;

    
    //Home Page
	@GetMapping("/car-management/home")
    public String showHomePage() {
        return "admin/car-management/home"; 
    }
	
	
	 @GetMapping("/car-model")
	    public String carModelPage() {
	        return "car-model"; // Thymeleaf will look for car-model.html in the templates folder
	  }
	
	
	//Register Page
	 @GetMapping("/car-register")
	    public String showCarRegisterPage(Model model) {
	        model.addAttribute("car", new Car()); // Add a new Car object to the model
	        return "admin/car-management/car-register";
	    }

	    @PostMapping("/register")
	    public String submitCarForm(@ModelAttribute Car car, BindingResult result, Model model) {
	        String BASE_URL = "http://localhost:7000"; // Define backend base URL
	      


	        try {
	            // Call backend API to add the car
	            ResponseEntity<String> response = restTemplate.postForEntity(
	                BASE_URL + "/addCar", // Backend endpoint
	                car,                 // Data to send
	                String.class         // Response type
	            );

	            // Handle successful response
	            model.addAttribute("successMessage", response.getBody());
	            return "redirect:/car-management/home"; // Redirect to home or success page
	        } catch (HttpClientErrorException e) {
	            // Parse backend validation errors
	            Map<String, String> errors = null;
	            try {
	                errors = new ObjectMapper().readValue(
	                    e.getResponseBodyAsString(),
	                    new TypeReference<Map<String, String>>() {}
	                );
	            } catch (JsonProcessingException ex) {
	                model.addAttribute("errorMessage", "An error occurred while parsing backend errors.");
	                return "admin/car-management/car-register";
	            }


	            // Map backend errors to BindingResult
	            for (Map.Entry<String, String> entry : errors.entrySet()) {
	                String field = entry.getKey();
	                String errorMessage = entry.getValue();
	                result.rejectValue(field, "", errorMessage);
	            }
	        } catch (Exception e) {
	            // Handle generic exceptions
	            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
	            return "admin/car-management/car-register";
	        }

	        // Stay on the registration page and display validation errors
	        return "admin/car-management/car-register";

	    }
	    
	    
	   
	    
	    //Filter Page
	    
	    @GetMapping("/filter")
		  public String filter(Model model) {
			  model.addAttribute("cars", new ArrayList<Car>());
			  return "admin/car-management/filter";
		  }
	    
	    
	    //Update page
	    
	    @GetMapping("/update-car")
	    public String showCarUpdatePage(Model model) {
	        model.addAttribute("car", new Car()); // Add Car object to model for form binding
	        return "admin/car-management/verify-reg";
	    }

	    @GetMapping("/update")
	    public String fetchCarDetails(@ModelAttribute Car car, Model model) {
	        String backendUrl = "http://localhost:7000/getCar/registrationnumber/" + car.getRegistrationNumber();

	        try {
	            ResponseEntity<Car> response = restTemplate.exchange(
	                backendUrl,
	                HttpMethod.GET,
	                null,
	                Car.class
	            );

	            Car fetchedCar = response.getBody();
	            if (fetchedCar != null) {
	                model.addAttribute("car", fetchedCar);
	                return "admin/car-management/car-update"; // Display update form with car details
	            }
	        } catch (HttpClientErrorException.NotFound e) {
	            // Handle 404 error and pass the backend error message to the template
	            String errorMessage = e.getResponseBodyAsString();
	            model.addAttribute("errorMessage", errorMessage);
	        } catch (Exception e) {
	            // Handle unexpected errors
	            model.addAttribute("errorMessage", "An error occurred while retrieving the car: " + e.getMessage());
	        }

	        return "admin/car-management/verify-reg"; // Stay on the verification page and display the error message
	    }


	    @PostMapping("/update-car")
	    public String updateCarDetails(@ModelAttribute Car car, BindingResult result, Model model) {
	        String backendUrl = "http://localhost:7000/updateCar";

	        try {
	            HttpHeaders headers = new HttpHeaders();
	            headers.set("Content-Type", "application/json");

	            HttpEntity<Car> request = new HttpEntity<>(car, headers);

	            ResponseEntity<Car> response = restTemplate.exchange(
	                backendUrl,
	                HttpMethod.PUT,
	                request,
	                Car.class
	            );

	            if (response.getStatusCode().is2xxSuccessful()) {
	                model.addAttribute("successMessage", "Car updated successfully!");
	                return "redirect:/car-management/home";
	            }
	        } catch (HttpClientErrorException.BadRequest e) {
	            // Parse backend validation errors
	            Map<String, String> errors = parseErrors(e);
	            if (errors != null) {
	                for (Map.Entry<String, String> entry : errors.entrySet()) {
	                    result.addError(new FieldError("car", entry.getKey(), entry.getValue()));
	                }
	            }
	        } catch (Exception e) {
	            model.addAttribute("errorMessage", "An unexpected error occurred while updating the car: " + e.getMessage());
	        }

	        return "admin/car-management/car-update";
	    }

	    private Map<String, String> parseErrors(HttpClientErrorException e) {
	        try {
	            ObjectMapper objectMapper = new ObjectMapper();
	            return objectMapper.readValue(e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
	        } catch (Exception ex) {
	            return null;
	        }
	    }
	    
	    
	    
	    @GetMapping("/search-car")
	    public String showCar(Model model) {
	        model.addAttribute("car", new Car()); // Add an empty Car object to the model
	        return "admin/car-management/search-car";
	    }
 
	    
	    //Get By Registration

	    @GetMapping("/getCar")
	    public String getCar(@ModelAttribute Car car, Model model) {
	        String registrationNumber = car.getRegistrationNumber(); // Extract registration number
	        String backendUrl = "http://localhost:7000/getCar/registrationnumber/" + registrationNumber;

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
	                return "car"; // Render car.html template
	            }
	        } catch (HttpClientErrorException.NotFound e) {
	            // Backend returns 404 when the car is not found
	            model.addAttribute("errorMessage", "Enter a valid registration number");
	        } catch (Exception e) {
	            // Handle unexpected errors
	            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
	        }

	        return "admin/car-management/search-car"; // Stay on the search page with the error message

	    }
	    
	    
	    //Find Car
	    
	    @GetMapping("/findCar")
	    public String getCar(@RequestParam String registrationNumber, Model model) {
	    
	        String backendUrl = "http://localhost:7000/getCar/registrationnumber/" + registrationNumber;

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
	                return "car"; // Render car.html template
	            }
	        } catch (HttpClientErrorException.NotFound e) {
	            // Backend returns 404 when the car is not found
	            model.addAttribute("errorMessage", "Enter a valid registration number");
	        } catch (Exception e) {
	            // Handle unexpected errors
	            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
	        }

	        return "admin/car-management/search-car"; // Stay on the search page with the error message

	    }
	    
	    
	    

}
