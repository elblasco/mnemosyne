document.getElementById("logIn").addEventListener("submit", remapAction("logInPsw"));
document.getElementById("signUp").addEventListener("submit", remapAction("signUpPsw"));

function remapAction(idPsw) {
    return async function(e) {
        e.preventDefault();
        digestMessage(idPsw).then(
            (digestHex) => sessionStorage.setItem("hashedPsw", digestHex)
        );
        e.target.submit();
    }
}

async function digestMessage(idPsw) {
    const plainPsw = document.getElementById(idPsw).value;
    const pswUint8 = new TextEncoder().encode(plainPsw);
    const hashBuffer = await window.crypto.subtle.digest("SHA-256", pswUint8);
    if (Uint8Array.prototype.toHex) {
        // Uint8Array.toHex() was introduced in September 2025 better to create a fallback
        return new Uint8Array(hashBuffer).toHex();
    }
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const hashHex = hashArray
        .map((b) => b.toString(16).padStart(2, "0"))
        .join("");
    return hashHex;
}