document.getElementById("loginForm").addEventListener("submit", async(e) => {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const pin = document.getElementById("pin").value;

    try {
        const response = await fetch("http://localhost:5000/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, pin })
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem("token", data.token);
            window.location.href = "dashboard.html";
        } else {
            alert(data.message);
        }
    } catch (error) {
        alert("Server not responding!");
    }
});