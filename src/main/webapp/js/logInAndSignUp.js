import {hashAndSave} from "./utils.js";

document.getElementById("logIn").addEventListener("submit", remapAction("logInUsr", "logInPsw", "LogIn"));
document.getElementById("signUp").addEventListener("submit", remapAction("signUpUsr", "signUpPsw", "SignUp"));

function remapAction(idUsr, idPsw, endPoint) {
    return async function (e) {
        e.preventDefault();
        await digestMessage(idUsr, idPsw);

        const form = document.createElement("form");
        form.method = "POST";
        form.action = endPoint;
        [
            ["username", sessionStorage.getItem("username")],
            ["pasHash", sessionStorage.getItem("pasHash")],
        ].forEach(([name, value]) => {
            const input = document.createElement("input");
            input.type = "hidden";
            input.name = name;
            input.value = value;
            form.appendChild(input);
        });

        document.body.appendChild(form);
        form.submit()
    }
}

async function digestMessage(idUsr, idPsw) {
    const plainPsw = document.getElementById(idPsw).value;
    const plainUSr = document.getElementById(idUsr).value;
    sessionStorage.setItem("username", plainUSr);
    await hashAndSave("keyEnc", (plainPsw + plainUSr));
    await hashAndSave("pasHash", plainPsw);
}