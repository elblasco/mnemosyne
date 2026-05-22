document.getElementById('LogOut').addEventListener(
    "submit",
    async (event) => {
        event.preventDefault();
        sessionStorage.clear();
        window.location.href = "../mnemosyne";
    }
);