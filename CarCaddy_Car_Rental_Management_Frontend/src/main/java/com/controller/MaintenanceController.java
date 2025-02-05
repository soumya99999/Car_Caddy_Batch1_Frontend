package com.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.Car;
import com.model.Maintenance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequestMapping
@Controller
public class MaintenanceController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/maintenance-management/home")
    public String index(Model model) {
        model.addAttribute("imageUrl", "/images/black-car.png");
        model.addAttribute("title", "Keep Your Car in Top Shape");
        model.addAttribute("description", "Track your car’s maintenance schedule, get reminders, and ensure it stays in optimal condition.");
        return "admin/maintenance-management/index6";
    }
    @GetMapping("/maintenance/register")
    public String showMaintenanceForm(Model model) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String resp = restTemplate.getForObject("http://localhost:8000/cars/ids", String.class);
            model.addAttribute("maintainance", new Maintenance());
            List<Long> carIds = objectMapper.readValue(resp, new TypeReference<List<Long>>() {
            });
            System.out.println(carIds);
            model.addAttribute("carIds", carIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/maintenance-management/create-maintenance6";
    }

    @PostMapping("/maintenance/delete/{id}")
    public String deleteRequest(@PathVariable Long id) {
        String url = "http://localhost:8000/maintenance/delete/" + id;
//        System.out.println(url);
        String response = restTemplate.getForObject(url, String.class);
        System.out.println(response);
        return "redirect:/maintenance/list";
    }

    @PostMapping("/maintenance/edit/{id}")
    public String updateRecord(@PathVariable Long id, @ModelAttribute("maintenance") Maintenance maintainance, Model model) {
        System.out.println("updated maintenance data : " + maintainance.toString());
        boolean flag = false;
//        if(maintainance.getCarId()<0){
//            flag = true;
//            model.addAttribute("carIdError","Car Id must be positive number");
//        }
        if (maintainance.getDescription().length() >= 255) {
            flag = true;
            model.addAttribute("DescriptionError", "Description cannot exceed 255 characte.");
        }
        if (maintainance.getMaintenanceStatus().length() < 3 || maintainance.getMaintenanceStatus().length() > 50) {
            flag = true;
            model.addAttribute("statusError", "Maintenance status must be between 3 and 50 characters..");
        }
        if (maintainance.getMaintenanceType().length() < 3 || maintainance.getMaintenanceType().length() > 50) {
            flag = true;
            model.addAttribute("typeError", "Maintenance type must be between 3 and 50 characters.");
        }
        if (flag) {
            return "/maintenance/edit/{id}(id=${id})";
        }
        String url = "http://localhost:8000/maintenance/edit/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Maintenance> request = new HttpEntity<>(maintainance, headers);
        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        return "redirect:/maintenance/list";
    }

    @PostMapping("/maintenance/register")
    public String submitMaintenanceForm(@ModelAttribute("maintainance") Maintenance maintainance, Model model) {

        boolean flag = false;
//        if(maintainance.getCar()<0){
//            flag = true;
//            model.addAttribute("carIdError","Car Id must be positive number");
//        }
        if (maintainance.getCar().getCarId() < 0) {
            flag = true;
            model.addAttribute("carIdError", "Car Id must be positive number");
        }
        if (maintainance.getDescription().length() >= 255) {
            flag = true;
            model.addAttribute("DescriptionError", "Description cannot exceed 255 characters.");
        }
        if (maintainance.getMaintenanceStatus().length() < 3 || maintainance.getMaintenanceStatus().length() > 50) {
            flag = true;
            model.addAttribute("statusError", "Maintenance status must be between 3 and 50 characters..");
        }
        if (maintainance.getMaintenanceType().length() < 3 || maintainance.getMaintenanceType().length() > 50) {
            flag = true;
            model.addAttribute("typeError", "Maintenance type must be between 3 and 50 characters.");
        }
        if (flag) {
            return "admin/maintenance-management/create-maintenance6";
        }
        
        System.out.println(maintainance);
        String url = "http://localhost:8000/maintenance/create";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Maintenance> request = new HttpEntity<>(maintainance, headers);
        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        System.out.println(response);
        return "redirect:/maintenance/list";
    }

    @GetMapping("/maintenance/list")
    public String viewMaintenanceRecords(Model model, @RequestParam(defaultValue = "") String msg) {
        String url = "http://localhost:8000/maintenance/data";
        try {
        	Maintenance[] maintenanceList = restTemplate.getForObject(url, Maintenance[].class); 
            model.addAttribute("records", maintenanceList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/maintenance-management/view-maintenance6"; // No redirect needed, render the template directly
    }

    @GetMapping("/maintenance/delete/{id}")
    public String deleteMaintenanceRecord(@PathVariable Long id, Model model) {
        String url = "localhost:8000/maintenance/delete/" + id;
        String response = restTemplate.getForObject(url, String.class);
        return "redirect:/maintenance/list";
    }


    @GetMapping("/maintenance/edit/{id}")
    public String showEditMaintenanceForm(@PathVariable("id") Long id, Model model) {
        String url = "http://localhost:8000/maintenance/data/" + id;
     

        try {
        	  ResponseEntity<Maintenance> response = restTemplate.exchange(
  	                url,
  	                HttpMethod.GET,
  	                null,
  	                Maintenance.class
  	            );

  	            Maintenance record = response.getBody();
            model.addAttribute("maintenance", record);
            System.out.println("Date :" + record.getDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/maintenance-management/edit-maintenance6"; // Return the edit form view
    }
}
