package org.jenkinsci.account.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

// page_url = http://localhost:8080/login
public class LoginPage {
    @FindBy(name = "userid")
    private WebElement usernameField;

    @FindBy(name = "password")
    private WebElement passwordField;

    @FindBy(xpath = "//button")
    private WebElement loginBtn;

    @FindBy(xpath = "//a[@href=\"passwordReset\"]")
    private WebElement forgotPasswordLink;

    @FindBy(xpath = "//a[@href=\"signup\"]")
    private WebElement signupLink;

    public LoginPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);

        clickLogin();
    }

    public void clickForgotPassword() {
        forgotPasswordLink.click();
    }

    public void clickSignup() {
        signupLink.click();
    }

    private void enterUsername(String username) {
        usernameField.sendKeys(username);
    }

    private void enterPassword(String password) {
        passwordField.sendKeys(password);
    }

    private void clickLogin() {
        loginBtn.click();
    }
}