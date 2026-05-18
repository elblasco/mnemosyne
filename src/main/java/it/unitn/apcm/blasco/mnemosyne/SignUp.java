package it.unitn.apcm.blasco.mnemosyne;

import it.unitn.apcm.blasco.mnemosyne.utils.User;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.*;

@WebServlet(name = "SignUp", value = "/SignUp")
@MultipartConfig
public class SignUp extends HttpServlet {

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
            usr = null;
            psw = null;
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(("Empty credentials").getBytes());
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (User.IsUserInDB(conn, usr)) {
                resp.getOutputStream().write(("User already exists").getBytes());
            } else {
                java.security.Security.addProvider(new BouncyCastleProvider());
                SecureRandom random = SecureRandom.getInstance("DEFAULT", "BC");
                byte[] salt = new byte[8];
                random.nextBytes(salt);

                new User(usr, hashPassword(psw, salt), salt)
                        .insertUserInDB(conn);
                addCookie("username", usr, resp);
                addCookie("pasHash", Hex.toHexString(psw), resp);
                resp.sendRedirect(req.getContextPath() + "/home.jsp");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(("User does not exist").getBytes());
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            System.out.println("Problem with Bouncy Castle");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Technical error").getBytes());
        } finally {
            usr = null;
            psw = null;
        }
    }
}
