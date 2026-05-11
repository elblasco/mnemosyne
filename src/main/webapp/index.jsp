<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>LogIn or SignUp</title>
</head>
<body>
<% String error = request.getParameter("error");%>
<% if (error != null) { %>
<h1 style="color: red;"><%= error %></h1>
<% } %>
<h1>LogIn</h1>
<form method="post" action="LogIn">
    <label for="logInUsr">Username:</label><br>
    <input type="text" id="logInUsr" name="logInUsr" placeholder="username" required><br>
    <label for="logInPsw">Password:</label><br>
    <input type="password" id="logInPsw" name="logInPsw" placeholder="password" required><br><br>
    <input type="submit" value="LogIn">
</form>
<h1>SignUp</h1>
<br/>
<form method="post" action="SignUp">
    <label for="signUpUsr">Username:</label><br>
    <input type="text" id="logInUsr" name="signUpUsr" placeholder="username" required><br>
    <label for="signUpPsw">Password:</label><br>
    <input type="password" id="signUpPsw" name="signUpPsw" placeholder="password" required><br><br>
    <input type="submit" value="SignUp">
</form>
</body>
</html>