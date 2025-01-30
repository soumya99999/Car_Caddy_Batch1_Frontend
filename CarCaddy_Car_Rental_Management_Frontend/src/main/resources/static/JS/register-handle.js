// Utility function to display error messages
/*function validateField(field, validationFn, errorMessage) {
    const errorElementId = `${field.id}-error`;
    document.getElementById(errorElementId)?.remove();

    if (!validationFn(field.value)) {
        // Create and insert error message
        const errorElement = document.createElement("div");
        errorElement.id = errorElementId;
        errorElement.className = "error-message";
        errorElement.textContent = errorMessage;
        field.insertAdjacentElement("afterend", errorElement);

        // Apply error styles to input field
        field.classList.add("error");
        field.classList.remove("valid");
        return false;
    } else {
        // Remove error styles if validation passes
        field.classList.remove("error");
        field.classList.add("valid");
        return true;
    }
}

// Function to show dynamic popup messages
function showPopup(message, isSuccess) {
    const popup = document.createElement("div");
    popup.className = `popup ${isSuccess ? "success" : "error-popup"}`;
    popup.textContent = message;
    document.body.appendChild(popup);
    setTimeout(() => popup.remove(), 3000);
}

// Validation functions for each field
const validators = {
	regNumber: value => /^(?=.*[A-Z])(?=.*\d)[A-Z0-9-]+$/.test(value.trim()), // Validate regNumber
	    model: value => /^[A-Za-z\s]+$/.test(value.trim()), // Model must be a string
	    company: value => /^[A-Za-z\s]+$/.test(value.trim()), // Company must be a string
	    mileage: value => /^[1-9]\d*(\.\d+)?$/.test(value.trim()), // Validate mileage to accept decimal or integer
	    seatingCapacity: value => Number(value) > 0, // Positive integer for seating capacity
	    fuelType: value => value.trim() !== "", // Fuel type must not be empty
	    currentStatus: value => value.trim() !== "", // Current status must not be empty
	    insuranceNumber: value => /^INS\d{10}$/.test(value.trim()), // Validate insurance number format
	    carCondition: value => /^[A-Za-z\s]+$/.test(value.trim()), // Car condition must be a string
	    rentalRate: value => /^[1-9]\d*(\.\d+)?$/.test(value.trim()), // Validate rental rate for positive decimal/integers
	    previousServiceDate: value => value.trim() !== "", // Previous service date must not be empty
	    nextServiceDate: value => value.trim() !== "", // Next service date must not be empty
	    maintenanceStatus: value => /^[A-Za-z\s]+$/.test(value.trim()) // Maintenance status must be a string
};

// Validate service dates
function validateServiceDates() {
    const prevDateField = document.getElementById("previousServiceDate");
    const nextDateField = document.getElementById("nextServiceDate");
    const prevDate = new Date(prevDateField.value);
    const nextDate = new Date(nextDateField.value);
    let isValid = true;

    // Remove existing error messages
    document.getElementById("previousServiceDate-error")?.remove();
    document.getElementById("nextServiceDate-error")?.remove();

    if (prevDate >= nextDate) {
        // Add error messages for invalid date order
        const prevError = document.createElement("div");
        prevError.id = "previousServiceDate-error";
        prevError.className = "error-message";
        prevError.textContent = "Previous service date must be earlier than the next service date.";
        prevDateField.insertAdjacentElement("afterend", prevError);

        const nextError = document.createElement("div");
        nextError.id = "nextServiceDate-error";
        nextError.className = "error-message";
        nextError.textContent = "Next service date must be later than the previous service date.";
        nextDateField.insertAdjacentElement("afterend", nextError);

        prevDateField.classList.add("error");
        nextDateField.classList.add("error");
        prevDateField.classList.remove("valid");
        nextDateField.classList.remove("valid");
        isValid = false;
    } else {
        prevDateField.classList.remove("error");
        prevDateField.classList.add("valid");
        nextDateField.classList.remove("error");
        nextDateField.classList.add("valid");
    }
    return isValid;
}

// Handle form validation on blur events
document.querySelectorAll("input, select").forEach(field => {
    field.addEventListener("blur", () => {
        const validator = validators[field.id];
        if (validator) {
            const errorMessage = generateErrorMessage(field.id);
            validateField(field, validator, errorMessage);
        }
    });
});

// Generate specific error messages
function generateErrorMessage(fieldId) {
    const messages = {
        regNumber: "Invalid registration number (format: AB12CD3456).",
        model: "Model must be a string.",
        company: "Company must be a string.",
        mileage: "Mileage must be a positive integer.",
        seatingCapacity: "Seating capacity must be a positive integer.",
        fuelType: "Fuel type must be selected.",
        currentStatus: "Current status must be selected.",
        insuranceNumber: "Invalid insurance number (format: INS followed by 10 digits).",
        carCondition: "Car condition must be a string.",
        rentalRate: "Rental rate must be a positive number.",
        previousServiceDate: "Previous service date is invalid.",
        nextServiceDate: "Next service date is invalid.",
        maintenanceStatus: "Maintenance status must be a string."
    };
    return messages[fieldId];
}

// Handle form submission

document.getElementById("registration-form").addEventListener("submit", event => {

    event.preventDefault();
    let isValid = true;

    // Validate each field in the form
    Object.keys(validators).forEach(fieldId => {
        const field = document.getElementById(fieldId);
        const errorMessage = generateErrorMessage(fieldId);
        if (!validateField(field, validators[fieldId], errorMessage)) {
            isValid = false;
        }
    });

    // Validate service dates
    if (!validateServiceDates()) {
        isValid = false;
    }


    // If form is valid, submit, else show error popup
    if (isValid) {
        showPopup("Form is valid. Submitting...", true);
        event.target.submit();

    } else {
        showPopup("Please correct errors in the form.", false);
    }
});*/
