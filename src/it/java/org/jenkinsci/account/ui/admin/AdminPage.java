package org.jenkinsci.account.ui.admin;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

// page_url = http://localhost:8080/admin/
public class AdminPage {
    @FindBy(name = "word")
    private WebElement searchInput;

    @FindBy(xpath = "//input[@value=\"Search\"]")
    private WebElement searchButton;


    public AdminPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }

    public void search(String query) {
        searchInput.sendKeys(query);
        searchButton.click();
    }
}