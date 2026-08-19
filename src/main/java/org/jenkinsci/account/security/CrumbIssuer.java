package org.jenkinsci.account.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.kohsuke.stapler.StaplerRequest;

public class CrumbIssuer extends org.kohsuke.stapler.CrumbIssuer {

    public static final String CRUMB_FIELD = "crumb";

    private static final byte[] KEY;

    static {
        KEY = new byte[32];
        new SecureRandom().nextBytes(KEY);
    }

    @Override
    public String issueCrumb(StaplerRequest request) {
        return hmac(request.getSession().getId());
    }

    public static String getCrumb(javax.servlet.http.HttpServletRequest request) {
        return hmac(request.getSession().getId());
    }

    public static String getCrumb(jakarta.servlet.http.HttpServletRequest request) {
        return hmac(request.getSession().getId());
    }

    /** Validates a submitted crumb against the expected value for this session (javax variant). */
    public static void validate(javax.servlet.http.HttpServletRequest request, String submitted) {
        javax.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SecurityException("Invalid or missing CSRF token. Please reload the page and try again.");
        }
        validateHmac(session.getId(), submitted);
    }

    /** Validates a submitted crumb against the expected value for this session (jakarta variant). */
    public static void validate(jakarta.servlet.http.HttpServletRequest request, String submitted) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SecurityException("Invalid or missing CSRF token. Please reload the page and try again.");
        }
        validateHmac(session.getId(), submitted);
    }

    private static void validateHmac(String sessionId, String submitted) {
        String expected = hmac(sessionId);
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
