package org.jenkinsci.account.ui.resetpassword;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// page_url = http://localhost:8080/confirmPasswordReset?token=...
public class ConfirmPasswordResetPage {
    @FindBy(name = "newPassword1")
    private WebElement newPasswordInput;

    @FindBy(name = "newPassword2")
    private WebElement confirmPasswordInput;

    @FindBy(xpath = "//button[@type='submit']")
    private WebElement submitButton;

    private final WebDriver driver;

    public ConfirmPasswordResetPage(WebDriver driver) {
        this.driver = driver;
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.name("newPassword1")));
        PageFactory.initElements(driver, this);
    }

    public void setNewPassword(String password, String confirm) {
        newPasswordInput.sendKeys(password);
        confirmPasswordInput.sendKeys(confirm);
        submitButton.click();
        // Wait for the form submission to complete and redirect to login
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.not(
                        ExpectedConditions.urlContains("confirmPasswordReset")));
    }
}
