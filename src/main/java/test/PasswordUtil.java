package test;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author Kohsuke Kawaguchi
 */
public class PasswordUtil {
    private static final SecureRandom random = new SecureRandom();

    /**
     * Java version of 'slappasswd'.
     * See http://www.securitydocs.com/library/3439
     */
    public static synchronized String hashPassword(String password) {
        try {
            byte[] salt = new byte[4];
            random.nextBytes(salt);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(password.getBytes());
            sha.update(salt);
            byte[] hash = sha.digest();

            return "{SSHA}"+ Base64.encodeBase64String(concat(hash,salt));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] r = new byte[a.length+b.length];
        System.arraycopy(a,0,r,0,a.length);
        System.arraycopy(b,0,r,a.length,b.length);
        return r;
    }
}
