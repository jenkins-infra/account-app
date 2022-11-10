package org.jenkinsci.account.ui.admin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

// page_url = http://localhost:8080/admin/passwordReset
public class AdminResetPasswordResultPage {
    @FindBy(xpath = "//p/code")
    private WebElement newPasswordText;

    public AdminResetPasswordResultPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    public String getNewPassword() {
        return newPasswordText.getText();
    }
}
