package org.jenkinsci.account.ui.admin;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import javax.mail.MessagingException;
import org.jenkinsci.account.ui.BaseTest;
import org.jenkinsci.account.ui.email.Emails;
import org.jenkinsci.account.ui.email.ReadInboundEmailService;
import org.jenkinsci.account.ui.login.LoginPage;
import org.jenkinsci.account.ui.myaccount.MyAccountPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResetPasswordAdminTest extends BaseTest {

    @Test
    void resetPasswordAsAdmin() throws MessagingException, IOException {
        openHomePage();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("kohsuke", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver);
        myAccountPage.clickAdminLink();

        AdminPage adminPage = new AdminPage(driver);
        adminPage.search("alice");

        Date timestampBeforeReset = new Date();

        AdminSearchPage adminSearchPage = new AdminSearchPage(driver);
        adminSearchPage.resetPassword();

        AdminResetPasswordResultPage resetPasswordResultPage = new AdminResetPasswordResultPage(driver);
        String newPassword = resetPasswordResultPage.getNewPassword();

        String emailContent = READ_INBOUND_EMAIL_SERVICE
                .retrieveEmail(
                        "bob@jenkins-ci.org",
                        Emails.RESET_PASSWORD_SUBJECT,
                        timestampBeforeReset
                );

        Matcher matcher = Emails.PASSWORD_EXTRACTOR.matcher(emailContent);
        boolean matches = matcher.find();
        assertThat(matches).isTrue();

        String passwordFromEmail = matcher.group(1);

        assertThat(newPassword).isEqualTo(passwordFromEmail);

        newSession();

        new LoginPage(driver).login("alice", newPassword);
        String pageTitle = driver.getTitle();
        assertThat(pageTitle).contains("Account");
    }

    private void newSession() {
        driver.quit();
        startBrowser();
        openHomePage();
    }
}
