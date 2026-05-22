import {createAndFillFormData, hashAndSave, manageErrorFromServer, writeErrorMessage} from "./utils.js";

document.getElementById("logIn").addEventListener("submit", remapAction("logInUsr", "logInPsw", "LogIn"));
document.getElementById("signUp").addEventListener("submit", remapAction("signUpUsr", "signUpPsw", "SignUp"));

function remapAction(idUsr, idPsw, endPoint) {
    return async function (e) {
        e.preventDefault();

        await digestMessage(idUsr, idPsw);

        const formData = createAndFillFormData([
            ["username", sessionStorage.getItem("username")],
            ["pasHash", sessionStorage.getItem("pasHash")]
        ]);

        try {
            const response = await fetch(endPoint, {
                method: "POST",
                body: formData,
                redirect: "manual"
            });

            if (response.type === "opaqueredirect") {
                window.location.href = "home.jsp";
            } else if (!response.ok) {
                await manageErrorFromServer(response);
            } else {
                const errorText = await response.text();
                if (errorText) {
                    writeErrorMessage(errorText);
                } else {
                    window.location.href = "home.jsp";
                }
            }
        } catch (error) {
            writeErrorMessage("Network error: " + error.message);
        }
    }
}

async function digestMessage(idUsr, idPsw) {
    const plainPsw = document.getElementById(idPsw).value;
    const plainUSr = document.getElementById(idUsr).value;
    await hashAndSave("username", plainUSr);
    await hashAndSave("keyEnc", (plainPsw + plainUSr));
    await hashAndSave("pasHash", plainPsw);
}