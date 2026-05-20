package it.unitn.apcm.blasco.mnemosyne.endpoints.file;


import com.google.gson.Gson;
import it.unitn.apcm.blasco.mnemosyne.utils.EncryptedFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "FileList", value = "/FileList")
@MultipartConfig
public class FileList extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(new Gson().toJson(EncryptedFile.getFileList(
                    new String(req.getPart("username").getInputStream().readAllBytes())))
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException | ServletException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}