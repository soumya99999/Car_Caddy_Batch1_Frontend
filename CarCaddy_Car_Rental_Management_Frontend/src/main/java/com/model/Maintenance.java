package com.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;


public class Maintenance {

	private Long maintenanceId;

	private Car car;
	
	private String maintenanceType;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate date;

	private Double maintenanceCost;

	private String maintenanceStatus;

	private String description;

	public Long getMaintenanceId() {
		return maintenanceId;
	}

	public void setMaintenanceId(Long maintenanceId) {
		this.maintenanceId = maintenanceId;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public String getMaintenanceType() {
		return maintenanceType;
	}

	public void setMaintenanceType(String maintenanceType) {
		this.maintenanceType = maintenanceType;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Double getMaintenanceCost() {
		return maintenanceCost;
	}

	public void setMaintenanceCost(Double maintenanceCost) {
		this.maintenanceCost = maintenanceCost;
	}

	public String getMaintenanceStatus() {
		return maintenanceStatus;
	}

	public void setMaintenanceStatus(String maintenanceStatus) {
		this.maintenanceStatus = maintenanceStatus;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
