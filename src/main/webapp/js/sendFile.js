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
        const formData = new FormData();
        formData.append("file", file);
        formData.append("hashedPsw", sessionStorage.getItem("hashedPsw"));
        const response = await fetch("UploadFile", {
            method: "POST",
            body: formData,
        });
        if (response.ok) {
            form.reset();
            await loadFiles();
        }
    }
);

async function loadFiles() {
    const response = await fetch("FileList");
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
    const formData = new FormData();
    formData.append("fileName", fileName);
    formData.append("hashedPsw", sessionStorage.getItem("hashedPsw"));
    const response = await fetch("FileList", {
        method: "POST",
        body: formData,
    });

    if (!response.ok) {
        await manageErrorFromServer(response);
        return;
    }

    const blob = await response.blob();
    const url  = URL.createObjectURL(blob);

    const a= document.createElement("a");
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
        headers: { "Content-Type": "text/plain" },
        body: fileName,
    });
    if (!response.ok) {
        await manageErrorFromServer(response);
        return;
    }
}

async function deleteFile(fileName) {
    const response = await fetch("DeleteFile", {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: fileName,
    });
    if (!response.ok) {
        await manageErrorFromServer(response);
    }
    await loadFiles();
}

async function manageErrorFromServer(response) {
    const message = await response.text();
    writeErrorMessage(message)
}

function writeErrorMessage(message) {
    document.getElementById("errorHeading")?.remove();
    const h1 = document.createElement("h1");
    h1.id = "errorHeading";
    h1.textContent = message;
    h1.style.color = "red";
    document.body.prepend(h1);
}

loadFiles();