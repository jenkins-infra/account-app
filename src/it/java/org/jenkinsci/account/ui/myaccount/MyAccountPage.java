package org.jenkinsci.account.ui.myaccount;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// page_url = http://localhost:8080/
public class MyAccountPage {
    @FindBy(xpath = "//a[@href=\"/admin\"]")
    private WebElement administerLink;

    @FindBy(xpath = "//a[@href=\"/myself\"]")
    private WebElement profileLink;

    private final WebDriverWait wait;

    public MyAccountPage(WebDriver driver, WebDriverWait wait) {
        this.wait = wait;
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@href=\"/myself\"]")));
        PageFactory.initElements(driver, this);
    }

    public void clickAdminLink() {
        wait.until(ExpectedConditions.elementToBeClickable(administerLink));
        administerLink.click();
    }

    public void clickProfileLink() {
        wait.until(ExpectedConditions.elementToBeClickable(profileLink));
        profileLink.click();
    }
}
