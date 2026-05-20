package it.unitn.apcm.blasco.mnemosyne.endpoints.user;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static it.unitn.apcm.blasco.mnemosyne.utils.User.areUserCredentialValid;
import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.generateCookie;

@WebServlet(name = "LogIn", value = "/LogIn")
@MultipartConfig
public class LogIn extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath());
    }


    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String usr = req.getParameter("username");
        String rawPsw = req.getParameter("pasHash");

        if (usr == null || usr.isEmpty() || rawPsw == null || rawPsw.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(("Empty credentials").getBytes());
            return;
        }

        try {
            byte[] psw = Hex.decode(rawPsw);
            if (areUserCredentialValid(usr, psw)) {
                resp.addCookie(generateCookie("username", usr));
                resp.addCookie(generateCookie("pasHash", Hex.toHexString(psw)));
                resp.sendRedirect(req.getContextPath() + "/home.jsp");
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getOutputStream().write(("Invalid credentials").getBytes());
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println("Problem with Bouncy Castle");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Technical error").getBytes());
        } finally {
            usr = null;
        }
    }
}
