package org.jenkinsci.account.ui.email;

import java.util.regex.Pattern;

public class Emails {

    public static final Pattern RESET_LINK_EXTRACTOR =
            Pattern.compile("(https?://\\S+confirmPasswordReset\\?token=[A-Za-z0-9_=-]+)");

    public static final String RESET_PASSWORD_SUBJECT = "Password reset on the Jenkins project infrastructure";
}
