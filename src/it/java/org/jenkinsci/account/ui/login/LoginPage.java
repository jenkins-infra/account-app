package org.jenkinsci.account.ui.login;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

    private final WebDriverWait wait;

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        this.wait = wait;
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                org.openqa.selenium.By.name("userid")));
        PageFactory.initElements(driver, this);
    }

    public void login(String username, String password) {
        wait.until(ExpectedConditions.elementToBeClickable(usernameField));
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginBtn.click();
    }

    public void clickForgotPassword() {
        wait.until(ExpectedConditions.elementToBeClickable(forgotPasswordLink));
        forgotPasswordLink.click();
    }

    public void clickSignup() {
        wait.until(ExpectedConditions.elementToBeClickable(signupLink));
        signupLink.click();
    }
}
