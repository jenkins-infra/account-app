package org.jenkinsci.account;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

/**
 * Indicates a problem in the user-given information.
 * Messages are considered untrusted and therefore escaped.
 *
 * @author Kohsuke Kawaguchi
 */
public class UserError extends RuntimeException implements HttpResponse {
    private String id;

    public UserError(String message) {
        super(message);
    }

    /**
     * @param message error message
     * @param id      ID for matching server logs with Jira issues
     */
    public UserError(String message, String id) {
        super(message);
        this.id = id;
    }

    @Override
    public void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node) throws IOException, ServletException {
        rsp.forward(this, "index", req);
    }

    /**
     * @return ID for matching server logs with Jira issues
     */
    public String getId() {
        return id;
    }
}
