package org.jenkinsci.account.ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

// page_url = http://localhost:8080/
public class MyAccountPage {
    @FindBy(xpath = "//a[@href=\"./admin\"]")
    private WebElement administerLink;

    @FindBy(xpath = "//a[@href=\"./myself\"]")
    private WebElement profileLink;

    public MyAccountPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    public void clickAdminLink() {
       administerLink.click();
    }

    public void clickProfileLink() {
        profileLink.click();
    }
}
