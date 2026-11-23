package org.jenkinsci.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class PasswordResetTokenService {
    public static final Duration RESET_TTL = Duration.ofHours(1);
    public static final Duration ACTIVATION_TTL = Duration.ofHours(24);

    private static final SecureRandom RANDOM = new SecureRandom();

    private record TokenRecord(String userId, Instant expiresAt) {}

    private final ConcurrentHashMap<String, TokenRecord> tokenStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userToToken = new ConcurrentHashMap<>();

    public String createToken(String userId, Duration ttl) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String oldToken = userToToken.get(userId);
        if (oldToken != null) tokenStore.remove(oldToken);
        tokenStore.put(token, new TokenRecord(userId, Instant.now().plus(ttl)));
        userToToken.put(userId, token);
        return token;
    }

    public String validateToken(String token) {
        if (token == null) throw new UserError("Invalid or expired link.");
        TokenRecord r = tokenStore.get(token);
        if (r == null || Instant.now().isAfter(r.expiresAt())) {
            if (r != null) {
                tokenStore.remove(token);
                userToToken.remove(r.userId(), token);
            }
            throw new UserError("This link is invalid or has expired. Please request a new one.");
        }
        return r.userId();
    }

    /**
     * Atomically consumes and validates the token. Only one concurrent caller can succeed;
     * subsequent calls with the same token get the invalid-or-expired error.
     */
    public String validateAndConsumeToken(String token) {
        if (token == null) throw new UserError("Invalid or expired link.");
        TokenRecord r = tokenStore.remove(token);
        if (r == null || Instant.now().isAfter(r.expiresAt())) {
            throw new UserError("This link is invalid or has expired. Please request a new one.");
        }
        userToToken.remove(r.userId());
        return r.userId();
    }
}
