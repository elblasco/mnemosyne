package it.unitn.apcm.blasco.mnemosyne.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/UploadFile", "/home.jsp", "/FileList", "/InvalidateTag"})
public class UploadFileFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var req = (HttpServletRequest) request;
        var resp = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        if (session.getAttribute("username") != null) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect( "/mnemosyne");
        }
    }
}
