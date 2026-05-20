document.getElementById('LogOut').addEventListener(
    "submit",
    async (event) => {
        event.preventDefault();
        sessionStorage.clear();
        const response = await fetch("LogOut", {
            method: "POST",
        });
        if (response.ok || response.redirected) {
            const data = await response.json();
            window.location.href = data.redirect;
        }
    }
);