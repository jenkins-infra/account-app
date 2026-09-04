package org.jenkinsci.account.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.kohsuke.stapler.StaplerRequest2;

public class CrumbIssuer extends org.kohsuke.stapler.CrumbIssuer {

    public static final String CRUMB_FIELD = "crumb";

    private static final byte[] KEY;

    static {
        KEY = new byte[32];
        new SecureRandom().nextBytes(KEY);
    }

    @Override
    public String getCrumbExpression() {
        return "document.head.dataset.crumbValue";
    }

    @Override
    public String issueCrumb(StaplerRequest2 request) {
        return hmac(request.getSession().getId());
    }

    public static String getCrumb(String sessionId) {
        return hmac(sessionId);
    }

    public static void validate(HttpServletRequest request, String submitted) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SecurityException("Invalid or missing CSRF token. Please reload the page and try again.");
        }
        String expected = hmac(session.getId());
        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                submitted != null ? submitted.getBytes(StandardCharsets.UTF_8) : new byte[0])) {
            throw new SecurityException("Invalid or missing CSRF token. Please reload the page and try again.");
        }
    }

    private static String hmac(String sessionId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(KEY, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(sessionId.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new SecurityException("Failed to compute CSRF token", e);
        }
    }
}
