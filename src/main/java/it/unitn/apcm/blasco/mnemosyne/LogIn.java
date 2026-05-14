package it.unitn.apcm.blasco.mnemosyne;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.*;

import it.unitn.apcm.blasco.mnemosyne.utils.User;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import static it.unitn.apcm.blasco.mnemosyne.utils.User.getUserFromDB;
import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.DB_URL;
import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.hashPassword;

@WebServlet(name = "LogIn", value = "/LogIn")
public class LogIn extends HttpServlet {

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
            resp.sendRedirect(req.getContextPath() + "/?error=Empty+credentials");
            usr = null;
            psw = null;
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            User user = getUserFromDB(conn, usr);
            String hashedPsw = hashPassword(psw, user.salt());
            if (user.hashedPassword().equals(hashedPsw)) {
                HttpSession session = req.getSession();
                session.setAttribute("username", usr);
                resp.sendRedirect(req.getContextPath() + "/home.jsp");
            }
            else {
                resp.sendRedirect(req.getContextPath() + "/?error=Invalid+credentials");
            }
        }
        catch (SQLException e) {
            resp.sendRedirect(req.getContextPath() + "/?error=User+does+not+exist");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println("Problem with Bouncy Castle");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.sendRedirect(req.getContextPath() + "/?error=Technical+error");
        } finally {
            usr = null;
            psw = null;
        }
    }
}
