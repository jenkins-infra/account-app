package org.jenkinsci.account.ui.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// page_url = http://localhost:8080/admin/passwordReset
public class AdminResetPasswordResultPage {
    @FindBy(xpath = "//p[strong]")
    private WebElement confirmationText;

    private final WebDriverWait wait;

    public AdminResetPasswordResultPage(WebDriver driver, WebDriverWait wait) {
        this.wait = wait;
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p/strong")));
        PageFactory.initElements(driver, this);
    }

    public String getConfirmationText() {
        return confirmationText.getText();
    }
}
