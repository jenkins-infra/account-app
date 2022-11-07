package org.jenkinsci.account.ui.admin;

import org.jenkinsci.account.ui.BaseTest;
import org.jenkinsci.account.ui.login.LoginPage;
import org.jenkinsci.account.ui.myaccount.MyAccountPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResetPasswordAdminTest extends BaseTest {

    @Test
    void resetPasswordAsAdmin() {
        driver.get("http://localhost:8080");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("kohsuke", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver);
        myAccountPage.clickAdminLink();

        AdminPage adminPage = new AdminPage(driver);
        adminPage.search("alice");

        AdminSearchPage adminSearchPage = new AdminSearchPage(driver);
        adminSearchPage.resetPassword();

        AdminResetPasswordResultPage resetPasswordResultPage = new AdminResetPasswordResultPage(driver);
        String newPassword = resetPasswordResultPage.getNewPassword();

        // TODO get password from email and assert same password

        newSession();

        new LoginPage(driver).login("alice", newPassword);
        String pageTitle = driver.getTitle();
        assertThat(pageTitle).contains("Account");
    }

    private void newSession() {
        driver.quit();
        startBrowser();
        driver.get("http://localhost:8080");
    }
}
