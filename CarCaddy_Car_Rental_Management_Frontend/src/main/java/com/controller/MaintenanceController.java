package com.controller;

import com.model.Maintenance;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequestMapping
@Controller
public class MaintenanceController {

	@Autowired
	private RestTemplate restTemplate;
	
    @GetMapping("/////")
    public String index() {
        return "index6";
    }

    @GetMapping("/maintenance")
    public String showMaintenanceForm(Model model) {
        model.addAttribute("maintainance", new Maintenance());
        return "create-maintenance6";
    }

    @PostMapping("/delete/{id}")
    public String deleteRequest(@PathVariable long id){
        String url = "http://localhost:7000/maintenance/delete/"+id;
//        System.out.println(url);
        String response = restTemplate.getForObject(url, String.class);
        System.out.println(response.toString());
        return "redirect:/maintenance/list";
    }

    @PostMapping("/maintenance/edit/{id}")
    public String updateRecord(@PathVariable("id") long id,@ModelAttribute("maintenance") Maintenance maintainance, Model model){
        System.out.println("updated maintenance data : "+maintainance.toString());
        boolean flag = false;
//        if(maintainance.getCarId()<0){
//            flag = true;
//            model.addAttribute("carIdError","Car Id must be positive number");
//        }
        if(maintainance.getDescription().length()>=255){
            flag = true;
            model.addAttribute("DescriptionError","Description cannot exceed 255 characte.");
        }
        if(maintainance.getMaintenanceStatus().length()<3||maintainance.getMaintenanceStatus().length()>50){
            flag = true;
            model.addAttribute("statusError","Maintenance status must be between 3 and 50 characters..");
        }
        if(maintainance.getMaintenanceType().length()<3||maintainance.getMaintenanceType().length()>50){
            flag = true;
            model.addAttribute("typeError","Maintenance type must be between 3 and 50 characters.");
        }
        if(flag) {
            return "/maintenance/edit/{id}(id=${id})";
        }
        String url = "http://localhost:7000/maintenance/edit/"+id;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Maintenance> request = new HttpEntity<>(maintainance, headers);
        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        return "redirect:/maintenance/list";
    }

    @PostMapping("////register")
    public String submitMaintenanceForm(@ModelAttribute("maintainance") @Validated Maintenance maintainance, Model model){
        System.out.println(maintainance.toString());
        boolean flag = false;
//        if(maintainance.getCar()<0){
//            flag = true;
//            model.addAttribute("carIdError","Car Id must be positive number");
//        }
        if(maintainance.getDescription().length()>=255){
            flag = true;
            model.addAttribute("DescriptionError","Description cannot exceed 255 characters.");
        }
        if(maintainance.getMaintenanceStatus().length()<3 || maintainance.getMaintenanceStatus().length()>50){
            flag = true;
            model.addAttribute("statusError","Maintenance status must be between 3 and 50 characters..");
        }
        if(maintainance.getMaintenanceType().length()<3 || maintainance.getMaintenanceType().length()>50){
            flag = true;
            model.addAttribute("typeError","Maintenance type must be between 3 and 50 characters.");
        }
        if(flag) {
        }
        String url = "http://localhost:7000/maintenance/create";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<Maintenance> request = new HttpEntity<>(maintainance, headers);
        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        System.out.println(response.toString());
        return "redirect:/maintenance/list";
    }

    @GetMapping("/maintenance/list")
    public String viewMaintenanceRecords(Model model ,@RequestParam(value = "msg", defaultValue = "") String msg) {
        String url = "http://localhost:7000/maintenance/data";
        String response = restTemplate.getForObject(url, String.class);
        // Convert the JSON response to a List of MaintenanceRecord objects
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Maintenance> maintenanceList = objectMapper.readValue(response, new TypeReference<List<Maintenance>>() {
            });
            model.addAttribute("records", maintenanceList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "view-maintenance6"; // No redirect needed, render the template directly
    }

    @GetMapping("/maintenance/delete/{id}")
    public String deleteMaintenanceRecord(@PathVariable("id") Integer id, Model model) {
        String url = "localhost:7000/maintenance/delete/" + id;
        String response = restTemplate.getForObject(url, String.class);
        return "redirect:/maintenance/list";
    }



    @GetMapping("/maintenance/edit/{id}")
    public String showEditMaintenanceForm(@PathVariable("id") Integer id, Model model) {
        String url = "http://localhost:7000/maintenance/data/"+id;
        String response = restTemplate.getForObject(url, String.class);
       // Convert the JSON response to a List of MaintenanceRecord objects
       ObjectMapper objectMapper = new ObjectMapper();
       try {
    	   Maintenance maintenance = objectMapper.readValue(response,new TypeReference<Maintenance>(){});
           model.addAttribute("maintenance", maintenance);
       } catch (Exception e) {
           e.printStackTrace();
       }
        return "edit-maintenance6"; // Return the edit form view
    }
}
