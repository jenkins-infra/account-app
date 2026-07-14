package org.jenkinsci.account.ui.resetpassword;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// page_url = http://localhost:8080/passwordReset
public class ResetPasswordPage {
    @FindBy(name = "id")
    private WebElement usernameOrEmailInput;

    @FindBy(xpath = "//button")
    private WebElement resetPrimaryBlockButton;

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ResetPasswordPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("id")));
        PageFactory.initElements(driver, this);
    }

    public void resetPassword(String usernameOrEmail) {
        wait.until(ExpectedConditions.elementToBeClickable(usernameOrEmailInput));
        usernameOrEmailInput.sendKeys(usernameOrEmail);
        resetPrimaryBlockButton.click();
    }

    public String resultText() {
        wait.until(ExpectedConditions.titleContains("Email sent!"));
        return driver.findElement(By.cssSelector("p")).getText();
    }
}
