package org.jenkinsci.account.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginTest extends BaseTest {

    @Test
    public void acceptsValidPassword() {
        driver.get("http://localhost:8080");

        LoginPage loginPage = new LoginPage(driver);

        loginPage.login("alice", "password");

        String pageTitle = driver.getTitle();
        assertThat(pageTitle).contains("Account");
    }
}
