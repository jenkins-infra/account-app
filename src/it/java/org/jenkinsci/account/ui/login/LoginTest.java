package org.jenkinsci.account.ui.login;

import org.jenkinsci.account.ui.BaseTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

class LoginTest extends BaseTest {

    @Test
    void acceptsValidPassword() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.login("alice", "password");

        wait.until(ExpectedConditions.titleContains("Account"));
        assertThat(driver.getTitle()).contains("Account");
    }
}
