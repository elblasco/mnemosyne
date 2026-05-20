package it.unitn.apcm.blasco.mnemosyne.endpoints.file;

import it.unitn.apcm.blasco.mnemosyne.utils.EncryptedFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;

@WebServlet(name = "InvalidateTag", value = "/InvalidateTag")
@MultipartConfig
public class InvalidateTag extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            EncryptedFile.invalidateTag(
                    new String(req.getPart("username").getInputStream().readAllBytes()),
                    new String(req.getPart("fileName").getInputStream().readAllBytes())
            );
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (IOException e) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Unable to handle request").getBytes());
        } catch (SQLException e) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("File does not exists").getBytes());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Error while invalidating the tag").getBytes());
        }
    }
}
