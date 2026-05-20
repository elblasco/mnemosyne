export async function hashAndSave(keyForSession, value) {
    const valUint8 = new TextEncoder().encode(value);
    const hashBuffer = await window.crypto.subtle.digest("SHA-256", valUint8);
    if (Uint8Array.prototype.toHex) {
        // Uint8Array.toHex() was introduced in September 2025, better to create a fallback
        sessionStorage.setItem(keyForSession, new Uint8Array(hashBuffer).toHex());
    } else {
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        const hashHex = hashArray
            .map((b) => b.toString(16).padStart(2, "0"))
            .join("");
        sessionStorage.setItem(keyForSession, hashHex);
    }
}

export function createAndFillFormData(tuplesNameValue) {
    const formData = new FormData();
    tuplesNameValue.forEach((element) => {
        formData.append(element[0], element[1]);
    });
    return formData;
}

export async function manageErrorFromServer(response) {
    const message = await response.text();
    writeErrorMessage(message)
}

export function writeErrorMessage(message) {
    document.getElementById("errorHeading")?.remove();
    const h1 = document.createElement("h1");
    h1.id = "errorHeading";
    h1.textContent = message;
    h1.style.color = "red";
    document.body.prepend(h1);
}