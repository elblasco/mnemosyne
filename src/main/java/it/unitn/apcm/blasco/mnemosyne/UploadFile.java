package it.unitn.apcm.blasco.mnemosyne;

import it.unitn.apcm.blasco.mnemosyne.utils.EncryptedFile;
import it.unitn.apcm.blasco.mnemosyne.utils.PlainFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.DB_URL;

@WebServlet(name = "UploadFile", value = "/UploadFile")
@MultipartConfig
public class UploadFile extends HttpServlet {

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

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part filePart = req.getPart("file");
        PlainFile plainFile = new PlainFile(
                filePart.getSubmittedFileName(),
                filePart.getInputStream().readAllBytes()
        );
        byte[] hashedPsw = Hex.decode(new String(req.getPart("hashedPsw").getInputStream().readAllBytes()));
        String userName = req.getSession().getAttribute("username").toString();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            new EncryptedFile(plainFile, hashedPsw, userName).insertIntoDB(conn);
            System.out.println(plainFile.name() + " has been added");
        }
        catch (SQLException e) {
            System.out.println("The request comes from " + req.getContextPath());
            resp.sendRedirect(req.getContextPath() + "?error=User+does+not+exist");
        } catch (InvalidCipherTextException e) {
            resp.sendRedirect(req.getContextPath() + "?error=Server+error");
        } finally {
            hashedPsw = null;
            userName = null;
            plainFile = null;
        }
    }
}
