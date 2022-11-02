package org.jenkinsci.account.ui.admin;

import org.jenkinsci.account.ui.BaseTest;
import org.jenkinsci.account.ui.login.LoginPage;
import org.jenkinsci.account.ui.myaccount.MyAccountPage;
import org.junit.jupiter.api.Test;

public class ResetPasswordAdminTest extends BaseTest {

    @Test
    void resetPasswordAsAdmin() {
        driver.get("http://localhost:8080");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("kohsuke", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver);
        myAccountPage.clickAdminLink();
    }
}
