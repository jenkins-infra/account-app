package org.jenkinsci.account.ui;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.chrome.ChromeDriver;

public class BaseTest {

    protected ChromeDriver driver;
    protected InMemoryDirectoryServer ds;

    @BeforeEach
    public void before() throws LDAPException {
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=jenkins-ci,dc=org");
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");

        InMemoryListenerConfig listener = InMemoryListenerConfig.createLDAPConfig("default", 1389);
        config.setListenerConfigs(listener);

        ds = new InMemoryDirectoryServer(config);

        // I wasn't able to load this as a resource for some reason, gradle not setting up resources properly maybe
        Path resource = Paths.get("src", "it", "resources", "org", "jenkinsci", "account", "ui", "test-data.ldif");
        Objects.requireNonNull(resource);

        ds.importFromLDIF(true, resource.toFile());
        ds.startListening();

        driver = new ChromeDriver();
    }

    @AfterEach
    public void after() {
        driver.quit();

        ds.close();
    }
}
