package it.unitn.apcm.blasco.mnemosyne.endpoints.file;

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

@WebServlet(name = "UploadFile", value = "/UploadFile")
@MultipartConfig
public class UploadFile extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part filePart = req.getPart("file");
        try (PlainFile plainFile = new PlainFile(
                filePart.getSubmittedFileName(),
                filePart.getInputStream().readAllBytes()
        )) {
            try (EncryptedFile encryptedFile = new EncryptedFile(
                    plainFile,
                    decodeHexBytes(req.getPart("keyEnc").getInputStream().readAllBytes()),
                    new String(req.getPart("username").getInputStream().readAllBytes())
            )) {
                encryptedFile.insertIntoDB();
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getOutputStream().write(("User does not exist").getBytes());
        } catch (
                InvalidCipherTextException | NoSuchProviderException | NoSuchAlgorithmException |
                InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                BadPaddingException | InvalidKeyException e
        ) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getOutputStream().write(("Server Error " + e).getBytes());
        }
    }
}
