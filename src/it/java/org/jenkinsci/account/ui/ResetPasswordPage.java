package org.jenkinsci.account.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

// page_url = http://localhost:8080/passwordReset
public class ResetPasswordPage {
    @FindBy(name = "id")
    private WebElement usernameOrEmailInput;

    @FindBy(xpath = "//button")
    private WebElement resetPrimaryBlockButton;


    private final WebDriver driver;

    public ResetPasswordPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        this.driver = driver;
    }

    public void resetPassword(String usernameOrEmail) {
        usernameOrEmailInput.sendKeys(usernameOrEmail);
        resetPrimaryBlockButton.click();
    }
    
    public String resultText() {
        return driver.findElement(By.cssSelector("p")).getText();
    }
}
