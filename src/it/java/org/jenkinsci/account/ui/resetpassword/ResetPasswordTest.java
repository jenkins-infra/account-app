package org.jenkinsci.account.ui.resetpassword;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.jenkinsci.account.ui.BaseTest;
import org.jenkinsci.account.ui.email.ReadInboundEmailService;
import org.jenkinsci.account.ui.login.LoginPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResetPasswordTest extends BaseTest {

    public static final Pattern PASSWORD_EXTRACTOR = Pattern.compile("Your temporary password is ([a-zA-Z0-9]+)");

    @Test
    void resetPasswordAsUser() throws MessagingException, IOException, InterruptedException {
        driver.get("http://localhost:8080");

        LoginPage loginPage = new LoginPage(driver);
        loginPage.clickForgotPassword();

        Date timestampBeforeReset = new Date();

        ResetPasswordPage resetPasswordPage = new ResetPasswordPage(driver);
        resetPasswordPage.resetPassword("alice");

        String text = resetPasswordPage.resultText();
        assertThat(text).contains("If your user account or email address exists");

        String emailContent = new ReadInboundEmailService("localhost", 1143)
                .retrieveEmail(
                        "bob@jenkins-ci.org",
                        "Password reset on the Jenkins project infrastructure",
                        timestampBeforeReset
                );

        assertThat(emailContent).isNotEmpty();

        Matcher matcher = PASSWORD_EXTRACTOR.matcher(emailContent);
        boolean matches = matcher.find();
        assertThat(matches).isTrue();

        String password = matcher.group(1);

        driver.get("http://localhost:8080");
        loginPage.login("alice", password);

        String pageTitle = driver.getTitle();
        assertThat(pageTitle).contains("Account");
    }
}
