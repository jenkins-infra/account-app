package org.jenkinsci.account.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class CsrfFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(CsrfFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String servletPath = httpRequest.getServletPath();
        if ("POST".equalsIgnoreCase(httpRequest.getMethod())
                && !servletPath.startsWith("/botdetectcaptcha")) {
            String submitted = httpRequest.getHeader(CrumbIssuer.CRUMB_FIELD);
            if (submitted == null) {
                submitted = httpRequest.getParameter(CrumbIssuer.CRUMB_FIELD);
            }

            try {
                CrumbIssuer.validate(httpRequest, submitted);
                chain.doFilter(request, response);
            } catch (SecurityException e) {
                LOGGER.warning("CSRF check failed for " + httpRequest.getRequestURI() + ": " + e.getMessage());
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {}
}
