package it.unitn.apcm.blasco.mnemosyne.filters;

import it.unitn.apcm.blasco.mnemosyne.utils.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.stream.Collectors;

@WebFilter(urlPatterns = {"/home.jsp"})
@MultipartConfig
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        var req = (HttpServletRequest) request;
        var resp = (HttpServletResponse) response;
        try {
            if (req.getCookies() == null || req.getCookies().length == 0) {
                ((HttpServletResponse) response).sendRedirect("/mnemosyne");
            } else if (cookieValid(req, resp)) {
                chain.doFilter(request, response);
            } else {
                ((HttpServletResponse) response).sendRedirect("/mnemosyne");
            }
        } catch (Exception e) {
            ((HttpServletResponse) response).sendRedirect("/mnemosyne");
        }
    }

    private boolean cookieValid(HttpServletRequest req, HttpServletResponse resp) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[] usr = Hex.decode(getSpecificCookieValue(resp, req.getCookies(), "username"));
        byte[] psw = Hex.decode(getSpecificCookieValue(resp, req.getCookies(), "pasHash"));
        try {
            return User.areUserCredentialValid(
                    usr,
                    psw
            );
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw e;
        }
    }

    private String getSpecificCookieValue(HttpServletResponse resp, Cookie[] cookies, String cookieName) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieName))
                .map(cookie -> {
                    String ret = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                    cookie.setValue("");
                    cookie.setMaxAge(0);
                    cookie.setPath(cookie.getPath());
                    resp.addCookie(cookie);
                    return ret;
                })
                .collect(Collectors.joining());
    }
}
