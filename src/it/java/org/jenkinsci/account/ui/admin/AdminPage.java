package org.jenkinsci.account.ui.admin;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.assertj.core.api.Assertions.assertThat;

// page_url = http://localhost:8080/admin/
public class AdminPage {
    @FindBy(name = "word")
    private WebElement searchInput;

    @FindBy(xpath = "//button[contains(text(), 'Search')]")
    private WebElement searchButton;

    private final WebDriver driver;
    private final WebDriverWait wait;

    public AdminPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("word")));
        PageFactory.initElements(driver, this);
    }

    public void verifyOnPage() {
        wait.until(ExpectedConditions.titleContains("Admin"));
        assertThat(driver.getTitle()).contains("Admin");
    }

    public void search(String query) {
        wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        searchInput.sendKeys(query);
        searchButton.click();
    }
}
