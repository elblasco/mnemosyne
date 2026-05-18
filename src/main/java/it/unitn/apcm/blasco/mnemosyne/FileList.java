package it.unitn.apcm.blasco.mnemosyne;


import com.google.gson.Gson;
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

@WebServlet(name = "FileList", value = "/FileList")
@MultipartConfig
public class FileList extends HttpServlet {

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(new Gson().toJson(EncryptedFile.getFileList(
                    conn,
                    new String(req.getPart("username").getInputStream().readAllBytes())))
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException | ServletException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}