package it.unitn.apcm.blasco.mnemosyne.endpoints.file;

import it.unitn.apcm.blasco.mnemosyne.utils.EncryptedFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DeleteFile", value = "/DeleteFile")
@MultipartConfig
public class DeleteFile extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            EncryptedFile.deleteEncryptedFile(
                    new String(req.getPart("username").getInputStream().readAllBytes()),
                    new String(req.getPart("fileName").getInputStream().readAllBytes())
            );
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException | ServletException e) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Malformed request").getBytes());
        } catch (SQLException e) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Error while deleting, try again").getBytes());
        }
    }
}
