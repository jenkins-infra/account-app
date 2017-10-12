package org.jenkinsci.account;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by Olivier Vernin on 11/10/17.
 * Indicate an error send from the system which contain trusted information (no xss vulnerability)
 * and therefor doesn't need to be escaped
 */
public class SystemError extends RuntimeException implements HttpResponse {
    public SystemError(String message){
        super(message);
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.forward(this,"index",req);
    }
}
