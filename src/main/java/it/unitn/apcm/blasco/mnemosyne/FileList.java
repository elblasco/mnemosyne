package it.unitn.apcm.blasco.mnemosyne;


import com.google.gson.Gson;
import it.unitn.apcm.blasco.mnemosyne.utils.EncryptedFile;
import it.unitn.apcm.blasco.mnemosyne.utils.PlainFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Hex;

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

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String username = req.getSession().getAttribute("username").toString();
        try(Connection conn = DriverManager.getConnection(DB_URL)){
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(new Gson().toJson(EncryptedFile.getFileList(conn, username)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getSession().getAttribute("username").toString();
        String fileName;
        byte[] hashedPsw;
        try (Connection conn = DriverManager.getConnection(DB_URL)){
            fileName = new String(req.getPart("fileName").getInputStream().readAllBytes());
            hashedPsw = Hex.decode(new String(req.getPart("hashedPsw").getInputStream().readAllBytes()));
            PlainFile returnFile = PlainFile.fromEncryptedFile(
                    EncryptedFile.getFromDB(conn, username, fileName),
                    hashedPsw
            );
            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + returnFile.name() + "\"");
            resp.setContentLength(returnFile.content().length);

            resp.getOutputStream().write(returnFile.content());
            resp.setStatus(HttpServletResponse.SC_OK);
            returnFile = null;
        }
        catch (InvalidCipherTextException e) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("File has been tampered").getBytes());
        } catch (ServletException | IOException e) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Unable to send file back").getBytes());
        } catch (SQLException e) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("File does not exists").getBytes());
        } finally {
            username = null;
            fileName = null;
            hashedPsw = null;
        }

    }
}