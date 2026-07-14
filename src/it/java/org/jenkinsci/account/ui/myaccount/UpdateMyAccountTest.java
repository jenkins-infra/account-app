package org.jenkinsci.account.ui.myaccount;

import org.jenkinsci.account.ui.BaseTest;
import org.jenkinsci.account.ui.login.LoginPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateMyAccountTest extends BaseTest {

    @Test
    public void updateProfileDetails() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.login("alice", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver, wait);
        myAccountPage.clickProfileLink();

        MyProfilePage profilePage = new MyProfilePage(driver, wait);
        profilePage.updateFirstName("Kohsuke1");
        profilePage.updateLastName("Kawaguchi1");
        profilePage.updateEmail("kohsuke@jenkins-ci.org");
        profilePage.updateGitHub("kohsuke1");
        profilePage.updateSSHKeys("abcdefgh");
        profilePage.update();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertThat(driver.findElement(By.tagName("h1")).getText()).isEqualTo("Done!");
    }


    @Test
    public void changePasswordValuesMustMatch() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.login("alice", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver, wait);
        myAccountPage.clickProfileLink();

        MyProfilePage profilePage = new MyProfilePage(driver, wait);
        profilePage.updateOldPassword("password");
        profilePage.updateNewPassword("password1");
        profilePage.updateConfirmNewPassword("password2");
        profilePage.update();

        wait.until(ExpectedConditions.titleContains("Error"));
        assertThat(driver.getTitle()).contains("Error");
    }

    @Test
    public void changePasswordValuesMustProvideOldPassword() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.login("alice", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver, wait);
        myAccountPage.clickProfileLink();

        MyProfilePage profilePage = new MyProfilePage(driver, wait);
        profilePage.updateNewPassword("password1");
        profilePage.updateConfirmNewPassword("password1");
        profilePage.update();

        wait.until(ExpectedConditions.titleContains("Error"));
        assertThat(driver.getTitle()).contains("Error");
    }

    @Test
    public void changePassword() {
        openHomePage();

        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.login("alice", "password");

        MyAccountPage myAccountPage = new MyAccountPage(driver, wait);
        myAccountPage.clickProfileLink();

        MyProfilePage profilePage = new MyProfilePage(driver, wait);
        profilePage.changePassword("password", "password1");

        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.titleContains("Account App"));
        newSession();
        loginPage = new LoginPage(driver, wait);
        loginPage.login("alice", "password1");

        wait.until(ExpectedConditions.titleContains("Account"));
        assertThat(driver.getTitle()).contains("Account");
    }
}
