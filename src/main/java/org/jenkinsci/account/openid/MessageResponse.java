package org.jenkinsci.account.openid;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.openid4java.message.Message;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * {@link Message} as {@link HttpResponse}
 *
 * @author Kohsuke Kawaguchi
 */
public class MessageResponse implements HttpResponse {
    private final Message msg;

    public MessageResponse(Message msg) {
        this.msg = msg;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.setContentType("text/plain");
        rsp.getWriter().print(msg.keyValueFormEncoding());
    }
}
