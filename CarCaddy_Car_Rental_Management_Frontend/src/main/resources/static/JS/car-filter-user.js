// Fetch all available cars when the page loads
window.onload = function() {
    fetchAvailableCars();
};

// Function to fetch all available cars
function fetchAvailableCars(queryString = '') {
    const url = queryString ? `http://localhost:8000/filter?${queryString}` : 'http://localhost:8000/filter';
    
    fetch(url, {
        method: 'GET',
    })
    .then(response => response.json())
    .then(data => {
        const resultsPanel = document.getElementById("results");
        resultsPanel.innerHTML = "";
        
        if (data.length === 0) {
            resultsPanel.innerHTML = "<p>No cars found.</p>";
            return;
        }

        data.forEach(car => {
            const card = document.createElement("div");
            card.className = "car-card";
            card.innerHTML = `
                <h3>${car.company} ${car.model}</h3>
                <p><strong>Registration Number:</strong> ${car.registrationNumber}</p>
                <p><strong>Mileage:</strong> ${car.mileage} km/l</p>
                <p><strong>Rental Rate:</strong> ₹${car.rentalRate} per day</p>
                <p><strong>Color:</strong> ${car.color}</p>
                <p><strong>Location:</strong> ${car.location}</p>
                <p><strong>Seating Capacity:</strong> ${car.seatingCapacity}</p>
            `;
            card.onclick = () => {
                window.location.href = `/user/getCar?registrationNumber=${car.registrationNumber}`;
            };
            resultsPanel.appendChild(card);
        });
    })
    .catch(error => console.error('Error fetching cars:', error));
}

// Function to handle input changes for filtering
function handleInputChange() {
    const company = document.getElementById("company").value;
    const mileage = document.getElementById("mileage").value;
    const rentalRate = document.getElementById("rentalRate").value;
    const color = document.getElementById("color").value;
    const location = document.getElementById("location").value;
    const seatingCapacity = document.getElementById("seatingCapacity").value;

    const queryString = `company=${company}&mileage=${mileage}&rentalRate=${rentalRate}&color=${color}&location=${location}&seatingCapacity=${seatingCapacity}`;
    
    fetchAvailableCars(queryString);
}
