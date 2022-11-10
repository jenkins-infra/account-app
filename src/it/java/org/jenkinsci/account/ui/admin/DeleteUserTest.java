package org.jenkinsci.account.ui.admin;

import org.jenkinsci.account.ui.BaseTest;
import org.jenkinsci.account.ui.login.LoginPage;
import org.jenkinsci.account.ui.myaccount.MyAccountPage;
import org.jenkinsci.account.ui.resetpassword.UserLookupType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteUserTest extends BaseTest {

    @Test
    public void deleteUserByUsername() {
        deleteUser("alice", "bob@jenkins-ci.org", UserLookupType.USERNAME);
    }

    @Test
    public void deleteUserByEmail() {
        deleteUser("alice", "bob@jenkins-ci.org", UserLookupType.EMAIL);
    }

    public void deleteUser(String username, String email, UserLookupType userLookupType) {
        openHomePage();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("kohsuke", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver);
        myAccountPage.clickAdminLink();

        AdminPage adminPage = new AdminPage(driver);
        if (userLookupType == UserLookupType.USERNAME) {
            adminPage.search(username);
        } else {
            adminPage.search(email);
        }

        AdminSearchPage adminSearchPage = new AdminSearchPage(driver);
        adminSearchPage.deleteUser();
        adminPage.verifyOnPage();

        newSession();

        loginPage = new LoginPage(driver);
        loginPage.login("alice", "password");
        assertThat(driver.getTitle()).isEqualTo("Error | Jenkins");
    }

    private void newSession() {
        driver.quit();
        startBrowser();
        openHomePage();
    }
}
