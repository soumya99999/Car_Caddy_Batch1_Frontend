package com.model;


import java.math.BigDecimal;

import java.math.RoundingMode;
import java.util.List;


import org.springframework.stereotype.Component;







@Component
public class Car {


    private Long carId;


    private String registrationNumber;


    private String model;

    private String company;


    private BigDecimal mileage;



    private Integer seatingCapacity;

    private String fuelType; 

  
    private String insuranceNumber;

    private String carCondition;


    private String currentStatus;

    private BigDecimal rentalRate;
    

    private String color;
    
    private String location;
    
    private List<Rental> bookings;
    
    private List<Maintenance> maintenance;

    

    
    

    public Car() {}

    // Getters and setters

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public BigDecimal getMileage() {
        return mileage;
    }


    public void setMileage(BigDecimal mileage) {
        if (mileage != null) {
            this.mileage = mileage.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.mileage = null;
        }
    }

    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getCarCondition() {
        return carCondition;
    }

    public void setCarCondition(String carCondition) {
        this.carCondition = carCondition;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public BigDecimal getRentalRate() {
        return rentalRate;
    }

    public void setRentalRate(BigDecimal rentalRate) {
        if (rentalRate != null) {
            this.rentalRate = rentalRate.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places
        } else {
            this.rentalRate = null;
        }
    }
    
    public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}


    

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<Rental> getBookings() {
		return bookings;
	}

	public void setBookings(List<Rental> bookings) {
		this.bookings = bookings;
	}

	public List<Maintenance> getMaintenance() {
		return maintenance;
	}

	public void setMaintenance(List<Maintenance> maintenance) {
		this.maintenance = maintenance;
	}
	
	



	
    
    

}
