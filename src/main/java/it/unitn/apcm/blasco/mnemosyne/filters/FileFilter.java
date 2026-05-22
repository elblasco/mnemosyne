package it.unitn.apcm.blasco.mnemosyne.filters;

import it.unitn.apcm.blasco.mnemosyne.utils.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static it.unitn.apcm.blasco.mnemosyne.utils.Utils.decodeHexBytes;

@WebFilter(urlPatterns = {"/UploadFile", "/FileList", "/InvalidateTag", "/LogOut", "/DeleteFile", "/DownLoadFile"})
@MultipartConfig
public class FileFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var req = (HttpServletRequest) request;
        try {
            if (!req.getParameterNames().hasMoreElements()) {
                ((HttpServletResponse) response).sendRedirect("/mnemosyne");
            } else if (User.areUserCredentialValid(
                    decodeHexBytes(req.getPart("username").getInputStream().readAllBytes()),
                    decodeHexBytes(req.getPart("pasHash").getInputStream().readAllBytes()))
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
