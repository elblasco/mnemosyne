package it.unitn.apcm.blasco.mnemosyne.filters;

import it.unitn.apcm.blasco.mnemosyne.utils.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

@WebFilter(urlPatterns = {"/UploadFile", "/FileList", "/InvalidateTag", "/LogOut", "/DeleteFile", "/DownLoadFile"})
@MultipartConfig
public class FileFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var req = (HttpServletRequest) request;
        try {
            if (User.areUserCredentialValid(
                    new String(req.getPart("username").getInputStream().readAllBytes()),
                    Hex.decode(new String(req.getPart("pasHash").getInputStream().readAllBytes())))
            ) {
                chain.doFilter(request, response);
            } else {
                ((HttpServletResponse) response).sendRedirect("/mnemosyne");
            }
        } catch (Exception e) {
            ((HttpServletResponse) response).sendRedirect("/mnemosyne");
        }
    }
}
