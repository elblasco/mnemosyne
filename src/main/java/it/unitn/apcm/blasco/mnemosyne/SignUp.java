package it.unitn.apcm.blasco.mnemosyne;

import it.unitn.apcm.blasco.mnemosyne.utils.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.*;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.DB_URL;
import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.hashPassword;

@WebServlet(name = "SignUp", value = "/SignUp")
public class SignUp extends HttpServlet {

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String usr = req.getParameter("signUpUsr");
        String psw = req.getParameter("signUpPsw");

        if (usr == null || usr.isEmpty() || psw == null || psw.isEmpty()) {
            usr = null;
            psw = null;
            resp.sendRedirect(req.getContextPath() + "/?error=Empty+credentials");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if(User.IsUserInDB(conn, usr)) {
                resp.sendRedirect(req.getContextPath() + "/?error=User+already+exists");
            } else {
                String salt = String.valueOf(System.currentTimeMillis() / 1000L);
                var user = new User(usr, hashPassword(psw, salt), salt);
                user.insertUserInDB(conn);
                System.out.println("User added successfully");
                HttpSession session = req.getSession();
                session.setAttribute("username", user.username());
                resp.sendRedirect(req.getContextPath() + "/home.jsp");
            }
        }
        catch (SQLException | NoSuchAlgorithmException e) {
            resp.sendRedirect(req.getContextPath() + "/?error=User+does+not+exist");
        } catch (NoSuchProviderException e) {
            System.out.println("Problem with Bouncy Castle");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.sendRedirect(req.getContextPath() + "/?error=Technical+error");
        } finally {
            usr = null;
            psw = null;
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}
