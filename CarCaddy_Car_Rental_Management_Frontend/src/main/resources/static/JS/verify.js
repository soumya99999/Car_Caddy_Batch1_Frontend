/*document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".verify-form");
    const registrationInput = document.getElementById("registration-number");
    const errorMessage = document.getElementById("error-message");

    form.addEventListener("submit", function (e) {
        const registrationValue = registrationInput.value.trim();
        const regEx = /^(?=.*[A-Z])(?=.*\d)[A-Z0-9-]+$/;

        if (!regEx.test(registrationValue)) {
            e.preventDefault(); // Prevent form submission
            errorMessage.style.display = "block"; // Show error message
            registrationInput.style.borderColor = "red"; // Highlight field
        } else {
            errorMessage.style.display = "none"; // Hide error message
            registrationInput.style.borderColor = "green"; // Reset field
        }
    });

    registrationInput.addEventListener("input", function () {
        errorMessage.style.display = "none"; // Hide error while typing
        registrationInput.style.borderColor = "#ccc"; // Reset border color
    });
});
*/