package it.unitn.apcm.blasco.mnemosyne.endpoints.user;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "LogOut", value = "/LogOut")
@MultipartConfig
public class LogOut extends HttpServlet {
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.sendRedirect("/");
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
