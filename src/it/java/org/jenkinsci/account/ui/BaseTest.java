package org.jenkinsci.account.ui;

import com.unboundid.ldap.listener.Base64PasswordEncoderOutputFormatter;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.SaltedMessageDigestInMemoryPasswordEncoder;
import com.unboundid.ldap.sdk.LDAPException;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class BaseTest {

    protected ChromeDriver driver;
    protected InMemoryDirectoryServer ds;

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

        InMemoryListenerConfig listener = InMemoryListenerConfig.createLDAPConfig("default", 1389);
        config.setListenerConfigs(listener);

        ds = new InMemoryDirectoryServer(config);

        // I wasn't able to load this as a resource for some reason, gradle not setting up resources properly maybe
        Path resource = Paths.get("src", "it", "resources", "org", "jenkinsci", "account", "ui", "test-data.ldif");
        Objects.requireNonNull(resource);

        ds.importFromLDIF(true, resource.toFile());
        ds.startListening();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
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

    @AfterEach
    public void after() {
        if (driver != null) {
            driver.quit();
        }

        if (ds != null) {
            ds.close();
        }
    }
}
