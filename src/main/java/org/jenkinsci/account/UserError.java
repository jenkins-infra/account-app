package org.jenkinsci.account;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Indicates a problem in the user given information.
 * Remark: Message are considered as untrusted and therefor are escaped.
 *
 * @author Kohsuke Kawaguchi
 */
public class UserError extends RuntimeException implements HttpResponse {
    private String id;

    public UserError(String message) {
        super(message);
    }

    /**
     * @param message
     *          error message
     * @param id
     *          ID for matching server logs with Jira issues
     */
    public UserError(String message, String id) {
        super(message);
        this.id = id;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.forward(this,"index",req);
    }

    /**
     * @return ID for matching server logs with Jira issues
     */
    public String getId(){
        return id;
    }
}
