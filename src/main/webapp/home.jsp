<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${sessionScope.username}'s home</title>
    <script src="./js/sendFile.js" defer></script>
</head>
<body>
<h1>${sessionScope.username}'s home</h1>
<%--<h1 id="client-error" style="color: red;"></h1>--%>
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
