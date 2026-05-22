package it.unitn.apcm.blasco.mnemosyne.endpoints.user;

import it.unitn.apcm.blasco.mnemosyne.utils.User;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.*;

@WebServlet(name = "SignUp", value = "/SignUp")
@MultipartConfig
public class SignUp extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath());
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rawUsr = req.getParameter("username");
        String rawPsw = req.getParameter("pasHash");

        if (rawUsr == null || rawUsr.isEmpty() || rawPsw == null || rawPsw.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(("Empty credentials").getBytes());
            return;
        }

        if (rawUsr.equals(rawPsw)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(("Cannot use the same string for username and password").getBytes());
            return;
        }

        try {
            byte[] psw = Hex.decode(rawPsw);
            byte[] usr = Hex.decode(rawUsr);
            if (User.isUsernameInDB(usr)) {
                resp.getOutputStream().write(("User already exists").getBytes());
            } else {
                byte[] salt = getRandomBytes(8);
                new User(usr, hashPassword(psw, salt), salt)
                        .insertUserInDB();
                resp.addCookie(generateCookie("username", rawUsr));
                resp.addCookie(generateCookie("pasHash", rawPsw));
                resp.sendRedirect(req.getContextPath() + "/home.jsp");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(("Error while inserting the user data " + e).getBytes());
        } catch (NoSuchProviderException | NoSuchAlgorithmException e) {
            System.out.println("Problem with Bouncy Castle");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Technical error " + e).getBytes());
        }
    }
}
