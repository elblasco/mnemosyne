package it.unitn.apcm.blasco.mnemosyne;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import static it.unitn.apcm.blasco.mnemosyne.Utils.DB_URL;
import static it.unitn.apcm.blasco.mnemosyne.Utils.User.getUserFromDB;
import static it.unitn.apcm.blasco.mnemosyne.Utils.hashPassword;

@WebServlet(name = "LogIn", value = "/LogIn")
public class LogIn extends HttpServlet {
    //private static final String DB_URL = "jdbc:sqlite:/data/mnemosyne.db";

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String usr = req.getParameter("logInUsr");
        String psw = req.getParameter("logInPsw");

        if (usr == null || usr.isEmpty() || psw == null || psw.isEmpty()) {
            System.out.println("Username or password is empty");
            resp.sendRedirect(req.getHeader("Referer"));
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Utils.User user = getUserFromDB(conn, usr);
            String hashedPsw = hashPassword(psw, user.Salt());
            if (user.hashedPassword().equals(hashedPsw)) {
                var out = resp.getWriter();
                out.println("<html><body><h1>Logged</h1></body></html>");
            }
            else {
                System.out.println("Logged in");
                req.setAttribute("username", usr);
                req.getRequestDispatcher("home.jsp").forward(req, resp);
            }
        }
        catch (SQLException |  NoSuchAlgorithmException e) {
            throw new IOException("Registration failed", e);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
