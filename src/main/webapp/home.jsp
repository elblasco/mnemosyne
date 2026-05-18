<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>File Management</title>
    <script src="./js/sendFile.js" type="module" defer></script>
    <script src="js/logOut.js" type="module" defer></script>
</head>
<body>
<h1>File Management</h1>
<form id="LogOut">
    <input type="submit" value="LogOut">
</form>
<h2>Upload a new file</h2>
<form id="sendFile">
    <input type="file" id="plainFile" name="plainFile"><br>
    <input type="submit" value="Upload file">
</form>
<h2>Files list</h2>
<ul id="fileList">
</ul>
</body>
</html>
