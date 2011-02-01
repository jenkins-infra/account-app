package org.jenkinsci.account;

/**
 * Indicates a problem in the user given information.
 *
 * TODO: render this nicely without rendering a stack trace.
 *
 * @author Kohsuke Kawaguchi
 */
public class UserError extends RuntimeException {
    public UserError(String message) {
        super(message);
    }
}
