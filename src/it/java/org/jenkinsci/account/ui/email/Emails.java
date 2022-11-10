package org.jenkinsci.account.ui.email;

import java.util.regex.Pattern;

public class Emails {

    public static final Pattern PASSWORD_EXTRACTOR = Pattern.compile("Your temporary password is ([a-zA-Z0-9]+)");

    public static final String RESET_PASSWORD_SUBJECT = "Password reset on the Jenkins project infrastructure";
}
