package com.model;
import java.time.LocalDate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;






public class Customer {

    private Long id;
    private String name;
    private Long phoneNumber;
    private String address;
    private String email;
    private String password;
    private int loyaltyPoints = 0;
    private boolean blacklistStatus = false;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dob; // Date of Birth
    private String drivingLicense; // Driving License
    private List<Rental> rentals;

    // Constructors
    public Customer() {
    }

    public Customer(String name, Long phoneNumber, String address, String email, String password, LocalDate dob, String drivingLicense) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.email = email;
        this.password = password;
        this.dob = dob;
        this.drivingLicense = drivingLicense;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public boolean isBlacklistStatus() {
        return blacklistStatus;
    }

    public void setBlacklistStatus(boolean blacklistStatus) {
        this.blacklistStatus = blacklistStatus;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getDrivingLicense() {
        return drivingLicense;
    }

    public void setDrivingLicense(String drivingLicense) {
        this.drivingLicense = drivingLicense;
    }
    
    

    public List<Rental> getRentals() {
		return rentals;
	}

	public void setRentals(List<Rental> rentals) {
		this.rentals = rentals;
	}

	// Utility Methods
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", loyaltyPoints=" + loyaltyPoints +
                ", blacklistStatus=" + blacklistStatus +
                ", dob=" + dob +
                ", drivingLicense='" + drivingLicense + '\'' +
                '}';
    }
}
