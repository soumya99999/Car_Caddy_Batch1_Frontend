const loginBtn = document.getElementById("login-btn");
const signupBtn = document.getElementById("signup-btn");
const nameField = document.getElementById("name-field");
const form = document.getElementById("auth-form");

// Default to Login mode
nameField.style.display = "none";

loginBtn.addEventListener("click", () => {
    nameField.style.display = "none"; // Hide name field for Login
    loginBtn.classList.add("active");
    signupBtn.classList.remove("active");
});

signupBtn.addEventListener("click", () => {
    nameField.style.display = "block"; // Show name field for Sign Up
    signupBtn.classList.add("active");
    loginBtn.classList.remove("active");
});


	form.addEventListener("submit", (event) => {
	    event.preventDefault();

	    // Fetch input values
	    const email = document.getElementById("email").value.trim();
	    const password = document.getElementById("password").value.trim();
	    const nameField = document.getElementById("name");
	    const isSignup = signupBtn.classList.contains("active");

	    // Regex Patterns
	    const emailPattern = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
	    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+={}\[\]:;"'<>,.?/\\|-])(?=.*\d)[A-Za-z\d!@#$%^&*()_+={}\[\]:;"'<>,.?/\\|-]{8,}$/;

	    // Validation
	    if (isSignup && (!nameField.value || nameField.value.trim() === "")) {
	        alert("Please enter your name.");
	        return;
	    }

	    if (!emailPattern.test(email)) {
	        alert("Please enter a valid email address (e.g., example@gmail.com).");
	        return;
	    }

	    if (!passwordPattern.test(password)) {
	        alert("Password must contain at least 8 characters, including uppercase, lowercase, numbers, and special characters.");
	        return;
	    }

	    // Prepare payload and URL
	    const url = isSignup
	        ? "http://localhost:8000/auth/admin/signup"
	        : "http://localhost:8000/auth/admin/login";

	    let options;

	    if (isSignup) {
	        // Use application/json for Signup
	        options = {
	            method: "POST",
	            headers: {
	                "Content-Type": "application/json",
	            },
	            body: JSON.stringify({
	                name: nameField.value.trim(),
	                email,
	                password,
	            }),
	        };
	    } else {
	        // Use application/x-www-form-urlencoded for Login
	        options = {
	            method: "POST",
	            headers: {
	                "Content-Type": "application/x-www-form-urlencoded",
	            },
	            body: new URLSearchParams({
	                email,
	                password,
	            }).toString(),
	        };
	    }

	    // Send data to backend
	    fetch(url, options)
	        .then((response) => {
	            if (response.ok) {
	                return response.text(); 
	            }
	            return response.text().then((error) => {
	                throw new Error(error || "Something went wrong.");
	            });
	        })
	        .then((data) => {
	            alert(isSignup ? "Signup successful!" : "Login successful!");
	            window.location.href = isSignup ? "/admin/login" : "/admin/home"; 
	        })
	        .catch((error) => {
	            alert(`Error: ${error.message}`);
	        });
	});
	

