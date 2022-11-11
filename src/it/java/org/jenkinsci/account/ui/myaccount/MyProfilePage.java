package org.jenkinsci.account.ui.myaccount;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

// page_url = http://localhost:8080/myself/
public class MyProfilePage {
    @FindBy(name = "firstName")
    private WebElement firstNameInput;

    @FindBy(name = "lastName")
    private WebElement lastNameInput;

    @FindBy(name = "email")
    private WebElement emailInput;

    @FindBy(name = "githubId")
    private WebElement githubInput;

    @FindBy(xpath = "//textarea")
    private WebElement sshKeysInput;

    @FindBy(name = "password")
    private WebElement currentPasswordInput;

    @FindBy(css = "input[name=\"newPassword1\"]")
    private WebElement newPasswordInput;

    @FindBy(css = "input[name=\"newPassword2\"]")
    private WebElement newPasswordConfirmInput;

    @FindBy(xpath = "//button[@type=\"submit\"]")
    private WebElement updateButton;

    public MyProfilePage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    public void updateFirstName(String text) {
        firstNameInput.clear();
        firstNameInput.sendKeys(text);
    }

    public void updateLastName(String text) {
        lastNameInput.clear();
        lastNameInput.sendKeys(text);
    }

    public void updateEmail(String text) {
        emailInput.clear();
        emailInput.sendKeys(text);
    }

    public void updateGitHub(String text) {
        githubInput.clear();
        githubInput.sendKeys(text);
    }

    public void updateSSHKeys(String text) {
        sshKeysInput.clear();
        sshKeysInput.sendKeys(text);
    }

    public void changePassword(String oldPassword, String newPassword) {
        updateOldPassword(oldPassword);
        updateNewPassword(newPassword);
        updateConfirmNewPassword(newPassword);

        updateButton.click();
    }

    public void updateConfirmNewPassword(String newPassword) {
        newPasswordConfirmInput.sendKeys(newPassword);
    }

    public void updateNewPassword(String newPassword) {
        newPasswordInput.sendKeys(newPassword);
    }

    public void updateOldPassword(String oldPassword) {
        currentPasswordInput.sendKeys(oldPassword);
    }

    public void update() {
        updateButton.click();
    }
}