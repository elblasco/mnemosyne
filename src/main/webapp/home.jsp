<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${username}'s home</title>
</head>
<body>
<h1>Upload a new file</h1>
<form action="UploadFile" method="post">
    <input type="file" id="plainFile" name="plainFile"><br>
    <input type="submit" value="Upload file">
</form>
<h1>Files list</h1>
</body>
</html>
