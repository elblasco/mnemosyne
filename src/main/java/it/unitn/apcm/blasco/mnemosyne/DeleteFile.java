package it.unitn.apcm.blasco.mnemosyne;

import it.unitn.apcm.blasco.mnemosyne.utils.EncryptedFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.DB_URL;

@WebServlet(name = "DeleteFile", value = "/DeleteFile")
@MultipartConfig
public class DeleteFile extends HttpServlet {

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            EncryptedFile.deleteEncryptedFile(
                    conn,
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
