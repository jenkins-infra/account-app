package org.jenkinsci.account.ui.myaccount;

import org.jenkinsci.account.ui.BaseTest;
import org.jenkinsci.account.ui.login.LoginPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateMyAccountTest extends BaseTest {

    @Test
    public void updateProfileDetails() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("alice", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver);
        myAccountPage.clickProfileLink();

        MyProfilePage profilePage = new MyProfilePage(driver);
        profilePage.updateFirstName("Kohsuke1");
        profilePage.updateLastName("Kawaguchi1");
        profilePage.updateEmail("kohsuke@jenkins-ci.org");
        profilePage.updateGitHub("kohsuke1");
        profilePage.updateSSHKeys("abcdefgh");
        profilePage.update();

        assertThat(driver.findElement(By.tagName("h1")).getText()).isEqualTo("Done!");
    }


    @Test
    public void changePasswordValuesMustMatch() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("alice", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver);
        myAccountPage.clickProfileLink();

        MyProfilePage profilePage = new MyProfilePage(driver);
        profilePage.updateOldPassword("password");
        profilePage.updateNewPassword("password1");
        profilePage.updateConfirmNewPassword("password2");
        profilePage.update();

        String pageTitle = driver.getTitle();
        assertThat(pageTitle).contains("Error");
    }

    @Test
    public void changePasswordValuesMustProvideOldPassword() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("alice", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver);
        myAccountPage.clickProfileLink();

        MyProfilePage profilePage = new MyProfilePage(driver);
        profilePage.updateNewPassword("password1");
        profilePage.updateConfirmNewPassword("password1");
        profilePage.update();

        String pageTitle = driver.getTitle();
        assertThat(pageTitle).contains("Error");
    }

    @Test
    public void changePassword() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("alice", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver);
        myAccountPage.clickProfileLink();

        MyProfilePage profilePage = new MyProfilePage(driver);
        profilePage.changePassword("password", "password1");

        newSession();
        loginPage = new LoginPage(driver);
        loginPage.login("alice", "password1");

        String pageTitle = driver.getTitle();
        assertThat(pageTitle).contains("Account");
    }
}
