package it.unitn.apcm.blasco.mnemosyne.endpoints.file;


import it.unitn.apcm.blasco.mnemosyne.utils.EncryptedFile;
import it.unitn.apcm.blasco.mnemosyne.utils.PlainFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.decodeHexBytes;
import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.decrypt;


@WebServlet(name = "DownLoadFile", value = "/DownLoadFile")
@MultipartConfig
public class DownLoadFile extends HttpServlet {

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (EncryptedFile encryptedFile = EncryptedFile.getFromDB(
                new String(req.getPart("username").getInputStream().readAllBytes()),
                new String(req.getPart("fileName").getInputStream().readAllBytes())
        )) {
            try (PlainFile plainFile = new PlainFile(
                    encryptedFile.fileName(),
                    decrypt(
                            encryptedFile.ciphertext(),
                            encryptedFile.tag(),
                            decodeHexBytes(req.getPart("keyEnc").getInputStream().readAllBytes()),
                            encryptedFile.nonce()
                    ))
            ) {
                resp.setContentType("application/octet-stream");
                resp.setHeader("Content-Disposition", "attachment; filename=\"" + encryptedFile.fileName() + "\"");
                resp.setContentLength(encryptedFile.ciphertext().length);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getOutputStream().write(plainFile.content());

            }
        } catch (BadPaddingException e) {
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
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Error decrypting the file").getBytes());
        }
    }
}