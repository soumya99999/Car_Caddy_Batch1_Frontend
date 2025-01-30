package com.controller;

import java.util.ArrayList;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.model.Car;

@Controller
public class UserController {
	
	@GetMapping("/")
    public String showLogInPage() {
        return "user/login"; 
    }

    @GetMapping("/user/home")
    public String userHome() {
        return "user/home"; // Renders user-home.html
    }
    @GetMapping("/user/filter")
	  public String filter(Model model) {
		  model.addAttribute("cars", new ArrayList<Car>());
		  return "user/filter";
	  }
    
    @GetMapping("/about")
    public String showAboutPage() {
        return "user/about"; 
    }

    @GetMapping("//contact")
    public String showContactPage() {
        return "user/contact"; 

    }
    
    @GetMapping("/bookings")
    public String showBookingsPage() {
        return "user/bookings"; 

    }
    
    @GetMapping("/profile")
    public String showProfilePage() {
        return "user/profile"; 

    }
}
