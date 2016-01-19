package org.jenkinsci.account;

import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Temporarily shut-off valve to disable sign-up.
 *
 * @author Kohsuke Kawaguchi
 */
public class CircuitBreaker {
    private final File file;

    public CircuitBreaker(File file) {
        this.file = file;
    }

    public CircuitBreaker(Parameters params) {
        String f = params.circuitBreakerFile();
        if (f!=null)
            this.file = new File(f);
        else
            this.file = new File("/no-such-file");
    }

    public boolean isOn() {
        return System.currentTimeMillis() < file.lastModified();
    }

    /**
     * Throws an exception if the circuit breaker is on.
     */
    public boolean check() throws IOException {
        if (isOn()) {
            LOGGER.info("Rejecting sign up due to circuit breaker");
            return true;
        }
        return false;
    }

    @RequirePOST
    public HttpResponse doSet(@QueryParameter String time) throws ParseException {
        Date t = makeFormatter().parse(time.trim());
        file.setLastModified(t.getTime());
        return HttpResponses.plainText("Successfully set");
    }

    /**
     * Current effective date
     */
    public String getDate() {
        long dt = file.lastModified();
        if (dt<=0)
            return "(none)";
        else
            return makeFormatter().format(new Date(dt));
    }

    private SimpleDateFormat makeFormatter() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm");
    }

    private static final Logger LOGGER = Logger.getLogger(CircuitBreaker.class.getName());
}
