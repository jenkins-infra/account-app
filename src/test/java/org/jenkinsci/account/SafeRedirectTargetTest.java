package org.jenkinsci.account;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafeRedirectTargetTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/myself",
            "/myself/",
            "/admin",
            "/admin/",
            "/admin/search?word=bob",
            "/admin/signup?userId=bob&firstName=B&lastName=L&email=b%40example.com",
    })
    void keepsPagesThatRequireLogin(String from) {
        assertEquals(from, Application.safeRedirectTarget(from));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "/",
            "/login",
            "/signup",
            "https://evil.example",
            "//evil.example",
            "///evil.example",
            "/\\evil.example",
            "/\\/evil.example",
            "/\t/evil.example",
            "//@evil.example",
            "/%2fevil.example",
            "/%5cevil.example",
            "//myself",
            "//admin/search?word=bob",
            "\\/admin",
            " /admin",
    })
    void sendsEverythingElseToRoot(String from) {
        assertEquals("/", Application.safeRedirectTarget(from));
    }
}
