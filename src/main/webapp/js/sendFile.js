import {createAndFillFormData, manageErrorFromServer, writeErrorMessage} from "./utils.js";

const form = document.getElementById("sendFile");

form.addEventListener(
    "submit",
    async (event) => {
        event.preventDefault();
        const files = document.getElementById("plainFile").files;
        if (files.length !== 1) {
            writeErrorMessage("Upload exactly one file")
            return;
        }
        const file = files[0];
        const response = await fetch("UploadFile", {
            method: "POST",
            body: createAndFillFormData([
                ["file", file],
                ["keyEnc", sessionStorage.getItem("keyEnc")],
                ["pasHash", sessionStorage.getItem("pasHash")],
                ["username", sessionStorage.getItem("username")]
            ]),
        });
        if (response.ok) {
            form.reset();
            await loadFiles();
        } else {
            await manageErrorFromServer(response);
        }
    }
);

async function loadFiles() {
    const response = await fetch("FileList", {
        method: "POST",
        body: createAndFillFormData([
            ["pasHash", sessionStorage.getItem("pasHash")],
            ["username", sessionStorage.getItem("username")]
        ]),
    });
    if (!response.ok) {
        document.getElementById("fileList").innerHTML =
            `<li class="error">Failed to load files</li>`;
        return;
    }
    const files = await response.json();
    populateList(files);
}

function populateList(items) {
    const list = document.getElementById("fileList");

    if (items.length === 0) {
        list.innerHTML = `<li>No files found</li>`;
        return;
    }

    list.innerHTML = "";

    items.forEach(item => {
        const li = document.createElement("li");
        const name = document.createTextNode(item + " ");

        const btn = document.createElement("button");
        btn.textContent = "Download";
        btn.onclick = () => downloadFile(item);

        const del = document.createElement("button");
        del.textContent = "Delete";
        del.onclick = () => deleteFile(item);

        const invalidate = document.createElement("button");
        invalidate.textContent = "Invalidate";
        invalidate.onclick = () => invalidateFile(item);

        li.appendChild(name);
        li.appendChild(btn);
        li.appendChild(del);
        li.appendChild(invalidate);
        list.appendChild(li);
        list.appendChild(li);
    });
}

async function downloadFile(fileName) {
    const response = await fetch("DownLoadFile", {
        method: "POST",
        body: createAndFillFormData([
            ["fileName", fileName],
            ["keyEnc", sessionStorage.getItem("keyEnc")],
            ["pasHash", sessionStorage.getItem("pasHash")],
            ["username", sessionStorage.getItem("username")]
        ]),
    });

    if (!response.ok) {
        await manageErrorFromServer(response);
        return;
    }

    const blob = await response.blob();
    const url = URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = fileName;
    a.style.display = "none";

    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

async function invalidateFile(fileName) {
    const response = await fetch("InvalidateTag", {
        method: "POST",
        body: createAndFillFormData([
            ["fileName", fileName],
            ["pasHash", sessionStorage.getItem("pasHash")],
            ["username", sessionStorage.getItem("username")]
        ]),
    });
    if (!response.ok) {
        await manageErrorFromServer(response);
    }
}

async function deleteFile(fileName) {
    const response = await fetch("DeleteFile", {
        method: "POST",
        body: createAndFillFormData([
            ["fileName", fileName],
            ["pasHash", sessionStorage.getItem("pasHash")],
            ["username", sessionStorage.getItem("username")]
        ]),
    });
    if (!response.ok) {
        await manageErrorFromServer(response);
    }
    await loadFiles();
}

await loadFiles();