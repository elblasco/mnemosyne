package it.unitn.apcm.blasco.mnemosyne;

import it.unitn.apcm.blasco.mnemosyne.utils.User;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static it.unitn.apcm.blasco.mnemosyne.utils.User.getUserFromDB;
import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.*;

@WebServlet(name = "LogIn", value = "/LogIn")
@MultipartConfig
public class LogIn extends HttpServlet {

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String usr = req.getParameter("username");
        byte[] psw = Hex.decode(req.getParameter("pasHash"));

        if (usr == null || usr.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(("Empty credentials").getBytes());
            usr = null;
            psw = null;
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            User user = getUserFromDB(conn, usr);
            if (MessageDigest.isEqual(
                    user.hashedPassword(),
                    hashPassword(psw, user.salt())
            )) {
                addCookie("username", usr, resp);
                addCookie("pasHash", Hex.toHexString(psw), resp);
                resp.sendRedirect(req.getContextPath() + "/home.jsp");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getOutputStream().write(("Invalid credentials").getBytes());
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(("User does not exist").getBytes());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println("Problem with Bouncy Castle");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Technical error").getBytes());
        } finally {
            usr = null;
            psw = null;
        }
    }
}
