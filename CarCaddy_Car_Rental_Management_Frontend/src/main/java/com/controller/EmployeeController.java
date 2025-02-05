package com.controller;

import java.util.List;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Employee;

import jakarta.servlet.http.HttpSession;

@Controller
public class EmployeeController {
	
	private final String backendUrl = "http://localhost:8000/api/employees";
	
	@Autowired
    private RestTemplate restTemplate;
	
	@GetMapping("/employee-management/home")
    public String showHomePage(Model model) {
		 Employee[] employees = restTemplate.getForObject(backendUrl + "/employees", Employee[].class);
	       model.addAttribute("employees", employees);
        return "admin/employee-management/employee-list"; 
    }
	

    @GetMapping("/admin/home")
    public String adminHome() {
        return "admin/home"; // Renders admin-home.html
    }
    
    @GetMapping("/admin/login")
    public String showALogInPage() {
        return "admin/login"; 
    }
    
    @GetMapping("/admin")
    public String entry() {
        return "admin/entry";
    }
    
    @GetMapping("/admin/profile")
    public String showProfile(Model model, HttpSession session) {
        Employee employee = (Employee) session.getAttribute("loggedInAdmin");
        if (employee == null) {
            return "redirect:/admin/login"; 
        }
        model.addAttribute("employee", employee);
        return "admin/profile"; // Thymeleaf template name
    }
    
    
    
  
    // Helper method to extract error message
    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(e.getResponseBodyAsString());
            return root.path("message").asText(); // Extract "message" field from JSON
        } catch (Exception ex) {
            return "An unexpected error occurred.";
        }
    }

  


    @GetMapping("/admin/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("employee", new Employee());
        return "admin/register";
    }

    @PostMapping("/admin/register")
    public String registerEmployee(@ModelAttribute Employee employee, BindingResult result, Model model) {
        String url = backendUrl + "/register";
        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Employee> request = new HttpEntity<>(employee, headers);
    
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            model.addAttribute("success", response.getBody());
            return "admin/login";
    
        } catch (HttpClientErrorException e) {
            // Parse and display validation errors from backend
            Map<String, String> errors=null;
					try {
						errors = new ObjectMapper().readValue(
						    e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
					} catch (JsonMappingException e1) {
						
						e1.printStackTrace();
					} catch (JsonProcessingException e1) {
					
						e1.printStackTrace();
					}
//				
			
				// Map backend errors to BindingResult				
				for(Map.Entry<String, String> entryset : errors.entrySet()) {
					String field = entryset.getKey();
					String errorMsg = entryset.getValue();							
					result.rejectValue(field,"",errorMsg);
				}
    
            return "admin/register";
        }
    }
    


    @PostMapping("/admin/login")
    public String login(@RequestParam String emailId, @RequestParam String password, Model model,HttpSession session) {
//        String url = backendUrl + "/login?emailId=" + emailId + "&password=" + password;
    	 String url = backendUrl + "/login/" + emailId + "/" + password;
        RestTemplate restTemplate = new RestTemplate();
        try {
            Employee employee = restTemplate.postForObject(url, null, Employee.class);

            if (employee != null && employee.getIsFirstLogin()) {
                model.addAttribute("emailId", emailId);
                return "admin/change-password";
            }

            session.setAttribute("loggedInAdmin", employee);
            return "admin/home";

        } catch (HttpClientErrorException e) {
            String errorMessage = extractErrorMessage(e);
            model.addAttribute("error", errorMessage);
            
            return "admin/login";
        }
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String emailId, @RequestParam String currentPassword,
                                 @RequestParam String newPassword, Model model) {
//        String url = backendUrl + "/update-password?emailId=" + emailId + "&newPassword=" + newPassword;
    	String url = backendUrl + "/update-password/" + emailId + "/" + newPassword;
        RestTemplate restTemplate = new RestTemplate();
        try {

            Employee employee = restTemplate.postForObject(url, null, Employee.class);
            model.addAttribute("success", "Password updated successfully. Please login again.");
            return "admin/login";

        } catch (HttpClientErrorException e) {
            String errorMessage = extractErrorMessage(e);
            model.addAttribute("error", errorMessage);
            return "admin/change-password";
        }
    }
    // View all employees
    @GetMapping("/admin/list")
    public String getAllEmployees(Model model) {
        Employee[] employees = restTemplate.getForObject(backendUrl + "/employees", Employee[].class);
        model.addAttribute("employees", employees);
        return "admin/employee-list";
    }

    // Show form to update employee details
    @GetMapping("/admin/update")
    public String showUpdateFormAdmin(Model model, HttpSession session) {
    	Employee employee = (Employee) session.getAttribute("loggedInAdmin");
        if (employee == null) {
            return "redirect:/admin/login"; 
        }
        model.addAttribute("employee", employee);
        return "admin/update-employee";
    }
    
    
    @PostMapping("/admin/update/{employeeId}")
    public String updateEmployeeAdmin(@PathVariable Long employeeId,
                                 @ModelAttribute("employee") Employee employee,
                                 BindingResult result,
                                 Model model,
                                 HttpSession session) {
        String url = backendUrl + "/updateEmployee/" + employeeId;
        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Employee> request = new HttpEntity<>(employee, headers);
    
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            
            session.setAttribute("loggedInAdmin", employee);
            
            return "redirect:/admin/profile"; // Redirect to the employee list after successful update
    
        } catch (HttpClientErrorException e) {
            // Parse and display validation errors from backend
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
            if (errors != null) {
                for (Map.Entry<String, String> entry : errors.entrySet()) {
                    String field = entry.getKey();
                    String errorMsg = entry.getValue();
                    result.rejectValue(field, "", errorMsg);
                }
            }
    
            // Add the employee object back to the model to retain form input
            model.addAttribute("employee", employee);
            return "admin/update-employee"; // Return to the update form with errors
        }
    }

    // Delete an employee
    @GetMapping("delete/employee/{employeeId}")
    public String deleteEmployee(@PathVariable Long employeeId) {
        restTemplate.delete(backendUrl + "/delete/" + employeeId);
        return "redirect:/employee-management/home";
    }
    
    
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalidate session
        return "redirect:/admin";  // Redirect to home page
    }
    
    
    @GetMapping("/update/employee/{employeeId}")
    public String showUpdateForm(@PathVariable Long employeeId, Model model) {
        Employee employee = restTemplate.getForObject(backendUrl + "/getEmployeeById/" + employeeId, Employee.class);
        model.addAttribute("employee", employee);
        return "admin/employee-management/update-employee";
    }
    
    @PostMapping("/update/employee/{employeeId}")
    public String updateEmployee(@PathVariable Long employeeId,
                                 @ModelAttribute("employee") Employee employee,
                                 BindingResult result,
                                 Model model) {
        String url = backendUrl + "/updateEmployee/" + employeeId;
        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Employee> request = new HttpEntity<>(employee, headers);
    
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            return "redirect:/employee-management/home"; // Redirect to the employee list after successful update
    
        } catch (HttpClientErrorException e) {
            // Parse and display validation errors from backend
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
            if (errors != null) {
                for (Map.Entry<String, String> entry : errors.entrySet()) {
                    String field = entry.getKey();
                    String errorMsg = entry.getValue();
                    result.rejectValue(field, "", errorMsg);
                }
            }
    
            // Add the employee object back to the model to retain form input
            model.addAttribute("employee", employee);
            return "admin/employee-management/update-employee"; // Return to the update form with errors
        }
    }

    
    
}
