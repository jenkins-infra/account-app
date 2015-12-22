package org.jenkinsci.account;

/**
 * Because I don't know how to expand schemas of slapd, we hijack
 * existing fields and use them for different purposes.
 *
 * @author Kohsuke Kawaguchi
 */
public class LdapAbuse {
    public static final String GITHUB_ID = "employeeNumber";
    public static final String SSH_KEYS = "preferredLanguage";
    public static final String REGISTRATION_DATE = "carLicense";
}
