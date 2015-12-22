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

    /**
     * "YYYY/MM/DD HH:MM:SS" that represents when the account was created.
     */
    public static final String REGISTRATION_DATE = "carLicense";

    /**
     * Represents the status of the seniority processing.
     * 'N' for newly created users who don't belong to the senior list.
     */
    public static final String SENIOR_STATUS = "businessCategory";
}
