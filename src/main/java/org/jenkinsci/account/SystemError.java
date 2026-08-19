package org.jenkinsci.account;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

/**
 * Indicates an error from the system containing trusted information (no XSS vulnerability)
 * and therefore doesn't need to be escaped.
 */
public class SystemError extends RuntimeException implements HttpResponse {
    public SystemError(String message) {
        super(message);
    }

    @Override
    public void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node) throws IOException, ServletException {
        rsp.forward(this, "index", req);
    }
}
