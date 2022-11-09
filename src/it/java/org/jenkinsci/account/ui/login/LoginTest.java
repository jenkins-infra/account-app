package org.jenkinsci.account.ui.login;

import org.jenkinsci.account.ui.BaseTest;
import org.jenkinsci.account.ui.login.LoginPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginTest extends BaseTest {

    @Test
    void acceptsValidPassword() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver);

        loginPage.login("alice", "password");

        String pageTitle = driver.getTitle();
        assertThat(pageTitle).contains("Account");
    }
}
