// Wait for the DOM to fully load
/*document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("updation-form");
    const updateButton = document.querySelector(".update");

    // Add event listener to the update button
    updateButton.addEventListener("click", (event) => {
        event.preventDefault(); // Prevent default form submission

        // Clear previous error messages
        clearErrorMessages();

        // Get all input and select fields in the form
        const inputs = form.querySelectorAll("input, select");
        let isFormValid = true;

        // Validate each field
        inputs.forEach((input) => {
            if (input.value.trim() !== "") {
                // Check validity of the field
                if (!validateField(input)) {
                    isFormValid = false;
                }
            }
        });

        // If the form is valid, show success popup and submit
        if (isFormValid) {
            displayPopup("Car updated successfully!", "success");
            setTimeout(() => {
                form.submit(); // Submit form after showing success popup
                form.reset(); // Reset form fields
            }, 2000);
        } else {
            displayPopup("Please correct the errors and try again.", "error");
        }
    });
});

// Function to validate a single field
function validateField(field) {
    const fieldName = field.id;
    let isValid = true;
    let errorMessage = "";

    // Validation logic for specific fields
    switch (fieldName) {
        case "regNumber":
            if (!/^(?=.*[A-Z])(?=.*\d)[A-Z0-9-]+$/.test(field.value)) {
                isValid = false;
                errorMessage = "Registration number must be a combination of uppercase letters and numbers (e.g., AP09AB1234 or AB1C23D).";
            }
            break;

        case "model":
        case "company":
        case "maintenanceStatus":
            if (!/^[A-Za-z ]+$/.test(field.value)) {
                isValid = false;
                errorMessage = `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} should contain only letters.`;
            }
            break;

        case "mileage":
        case "rentalRate":
            if (!/^\d+(\.\d+)?$/.test(field.value)) {
                isValid = false;
                errorMessage = "Please enter a valid number.";
            }
            break;

        case "seatingCapacity":
            if (field.value <= 0 || !/^\d+$/.test(field.value)) {
                isValid = false;
                errorMessage = "Seating capacity should be a positive whole number.";
            }
            break;

        case "insuranceNumber":
            if (!/^INS\d{10}$/.test(field.value)) {
                isValid = false;
                errorMessage = "Insurance number must be (e.g., INS12344764 ).";
            }
            break;

        case "fuelType":
        case "currentStatus":
        case "carCondition":
            if (!field.value) {
                isValid = false;
                errorMessage = "This field is required.";
            }
            break;

        case "previousServiceDate":
        case "nextServiceDate":
            const prevDate = document.getElementById("previousServiceDate").value;
            const nextDate = document.getElementById("nextServiceDate").value;

            if (prevDate && nextDate && new Date(prevDate) >= new Date(nextDate)) {
                isValid = false;
                errorMessage =
                    fieldName === "previousServiceDate"
                        ? "Previous service date must be earlier than next service date."
                        : "Next service date must be later than previous service date.";
            }
            break;

        default:
            break;
    }

    // Show error message if field is invalid
    if (!isValid) {
        displayErrorMessage(field, errorMessage);
    }

    return isValid;
}

// Function to clear all error messages
function clearErrorMessages() {
    const errorMessages = document.querySelectorAll(".error");
    errorMessages.forEach((error) => error.remove());
}

// Function to display an error message below a field
function displayErrorMessage(field, message) {
    // Create an error message element
    const error = document.createElement("div");
    error.className = "error";
    error.textContent = message;

    // Append the error message after the field
    field.parentNode.insertBefore(error, field.nextSibling);
}

// Function to display a popup message
function displayPopup(message, type) {
    // Create the popup container
    const popup = document.createElement("div");
    popup.className = `popup ${type}`; // Add dynamic class for styling
    popup.textContent = message;

    // Add popup to the body
    document.body.appendChild(popup);

    // Style the popup dynamically
    popup.style.position = "fixed";
    popup.style.bottom = "20px";
    popup.style.right = "20px";
    popup.style.padding = "15px 20px";
    popup.style.borderRadius = "8px";
    popup.style.backgroundColor = type === "success" ? "#28a745" : "#dc3545"; // Green for success, red for error
    popup.style.color = "white";
    popup.style.boxShadow = "0 4px 8px rgba(0, 0, 0, 0.2)";
    popup.style.fontSize = "14px";
    popup.style.fontWeight = "bold";
    popup.style.zIndex = "1000";
    popup.style.opacity = "1";
    popup.style.transition = "opacity 0.5s ease-in-out";

    // Fade out and remove popup after 3 seconds
    setTimeout(() => {
        popup.style.opacity = "0";
        setTimeout(() => popup.remove(), 500);
    }, 3000);
}
*/