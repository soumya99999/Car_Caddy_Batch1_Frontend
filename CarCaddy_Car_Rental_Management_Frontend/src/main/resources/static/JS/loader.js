document.addEventListener("DOMContentLoaded", () => {
    const loaderContainer = document.querySelector(".loader-container");
    setTimeout(() => {
        loaderContainer.style.display = "none"; // Hides the loader after a short delay
    }, 2000); // Adjust the timeout as needed
});
