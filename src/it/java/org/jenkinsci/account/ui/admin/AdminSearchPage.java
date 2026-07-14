package org.jenkinsci.account.ui.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// page_url = http://localhost:8080/admin/search
public class AdminSearchPage {
    @FindBy(css = "input[value$=\"password\"]")
    private WebElement resetPasswordButton;

    @FindBy(name = "email")
    private WebElement updateEmailInput;

    @FindBy(css = "input[value=\"Update\"]")
    private WebElement updateEmailButton;

    @FindBy(name = "confirm")
    private WebElement confirmDeleteUserInput;

    @FindBy(xpath = "//input[@value=\"Delete\"]")
    private WebElement deleteUserButton;

    private final WebDriverWait wait;

    public AdminSearchPage(WebDriver driver, WebDriverWait wait) {
        this.wait = wait;
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("confirm")));
        PageFactory.initElements(driver, this);
    }

    public void resetPassword() {
        wait.until(ExpectedConditions.elementToBeClickable(resetPasswordButton));
        resetPasswordButton.click();
    }

    public void deleteUser() {
        wait.until(ExpectedConditions.elementToBeClickable(confirmDeleteUserInput));
        confirmDeleteUserInput.sendKeys("Yes");
        deleteUserButton.click();
    }

    public void updateEmail(String newEmail) {
        wait.until(ExpectedConditions.elementToBeClickable(updateEmailInput));
        updateEmailInput.sendKeys(newEmail);
        updateEmailButton.click();
    }
}
