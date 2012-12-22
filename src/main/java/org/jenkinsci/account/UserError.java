package org.jenkinsci.account;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Indicates a problem in the user given information.
 *
 * @author Kohsuke Kawaguchi
 */
public class UserError extends RuntimeException implements HttpResponse {
    public UserError(String message) {
        super(message);
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.setContentType("text/plain");
        rsp.setStatus(500);
        rsp.getWriter().println(getMessage());
    }
}
