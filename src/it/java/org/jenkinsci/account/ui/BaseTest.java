package org.jenkinsci.account.ui;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.unboundid.ldap.listener.Base64PasswordEncoderOutputFormatter;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.SaltedMessageDigestInMemoryPasswordEncoder;
import com.unboundid.ldap.sdk.LDAPException;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.account.ui.email.ReadInboundEmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@ExtendWith(BaseTest.ScreenShotOnFailedTestExtension.class)
public class BaseTest {

    public ChromeDriver driver;
    protected InMemoryDirectoryServer ds;

    @RegisterExtension
    public static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    public static final ReadInboundEmailService READ_INBOUND_EMAIL_SERVICE = new ReadInboundEmailService("localhost", 3143);

    @BeforeAll
    static void setupAll() {
        WebDriverManager.chromiumdriver().setup();
    }

    @BeforeEach
    void before() throws LDAPException, NoSuchAlgorithmException {
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=jenkins-ci,dc=org");
        config.setPasswordEncoders(
                new SaltedMessageDigestInMemoryPasswordEncoder("{SSHA}",
                        Base64PasswordEncoderOutputFormatter.getInstance(),
                        MessageDigest.getInstance("SHA-1"),
                        4,
                        true,
                        true
                ));
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");

        InMemoryListenerConfig listener = InMemoryListenerConfig.createLDAPConfig("default", 3389);
        config.setListenerConfigs(listener);

        ds = new InMemoryDirectoryServer(config);

        // I wasn't able to load this as a resource for some reason, gradle not setting up resources properly maybe
        Path resource = Paths.get("src", "it", "resources", "org", "jenkinsci", "account", "ui", "test-data.ldif");
        Objects.requireNonNull(resource);

        ds.importFromLDIF(true, resource.toFile());
        ds.startListening();

        startBrowser();
    }

    public void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
    }

    public void openHomePage() {
        driver.get(System.getProperty("gretty.httpBaseURI"));
    }

    public void newSession() {
        driver.quit();
        startBrowser();
        openHomePage();
    }

    /**
     * Useful to use when you want to debug a test interactively
     */
    @SuppressWarnings({"unused", "InfiniteLoopStatement", "BusyWait"})
    public void pause() throws InterruptedException {
        while (true) {
            Thread.sleep(2000L);
        }
    }

    public void takeScreenshot(String testName) {
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(
                    scrFile,
                    new File(String.format("errorScreenshots/%s-%s.jpg", testName, UUID.randomUUID())
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void after() {
        if (driver != null) {
            driver.quit();
        }

        if (ds != null) {
            ds.close();
        }
    }

    public static class ScreenShotOnFailedTestExtension implements AfterTestExecutionCallback {

        @Override
        public void afterTestExecution(ExtensionContext context) throws Exception {
            boolean testFailed = context.getExecutionException().isPresent();

            if (testFailed) {
                BaseTest baseTest = (BaseTest) context.getRequiredTestInstance();
                Optional<Method> testMethod = context.getTestMethod();

                String displayName = testMethod
                        .map(Method::getName).orElse(context
                                .getDisplayName()
                                .replace("(", "")
                                .replace(")", ""));

                baseTest.takeScreenshot(displayName);
            }

        }
    }
}
