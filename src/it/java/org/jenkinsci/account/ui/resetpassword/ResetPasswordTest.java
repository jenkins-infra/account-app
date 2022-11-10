package org.jenkinsci.account.ui.resetpassword;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import org.jenkinsci.account.ui.BaseTest;
import org.jenkinsci.account.ui.email.Emails;
import org.jenkinsci.account.ui.email.ReadInboundEmailService;
import org.jenkinsci.account.ui.login.LoginPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResetPasswordTest extends BaseTest {

    @Test
    void resetPasswordWithUsername() throws MessagingException, IOException {
        resetPassword("alice", "bob@jenkins-ci.org", ResetPasswordType.USERNAME);
    }

    @Test
    void resetPasswordWithEmail() throws MessagingException, IOException {
        resetPassword("alice", "bob@jenkins-ci.org", ResetPasswordType.EMAIL);
    }

    private void resetPassword(String username, String email, ResetPasswordType resetPasswordType) throws MessagingException, IOException {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.clickForgotPassword();

        Date timestampBeforeReset = new Date();

        ResetPasswordPage resetPasswordPage = new ResetPasswordPage(driver);
        if (resetPasswordType == ResetPasswordType.USERNAME) {
            resetPasswordPage.resetPassword(username);
        } else {
            resetPasswordPage.resetPassword(email);
        }

        String text = resetPasswordPage.resultText();
        assertThat(text).contains("If your user account or email address exists");

        String emailContent = READ_INBOUND_EMAIL_SERVICE
                .retrieveEmail(
                        email,
                        Emails.RESET_PASSWORD_SUBJECT,
                        timestampBeforeReset
                );

        assertThat(emailContent).isNotEmpty();

        Matcher matcher = Emails.PASSWORD_EXTRACTOR.matcher(emailContent);
        boolean matches = matcher.find();
        assertThat(matches).isTrue();

        String password = matcher.group(1);

        openHomePage();
        loginPage.login(username, password);

        String pageTitle = driver.getTitle();
        assertThat(pageTitle).contains("Account");
    }
}
