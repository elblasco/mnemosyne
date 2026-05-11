const form = document.getElementById("sendFile");

async function sendData() {
    // Associate the FormData object with the form element
    const formData = new FormData(form);
    return JSON.stringify(formData);
}

form.addEventListener("submit", (event) => {
    event.preventDefault();
    sendData().then(r => document.writeln(r.toString()));
});