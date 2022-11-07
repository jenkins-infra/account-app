package org.jenkinsci.account.ui.admin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

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

    public AdminSearchPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    public void resetPassword() {
        resetPasswordButton.click();
    }

    public void deleteUser() {
        confirmDeleteUserInput.sendKeys("Yes");
        deleteUserButton.click();
    }

    public void updateEmail(String newEmail) {
        updateEmailInput.sendKeys(newEmail);
        updateEmailButton.click();
    }
}