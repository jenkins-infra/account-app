plugins {
    java
    `maven-publish`
    war
    id("org.gretty") version "3.0.9"
}

group = "org.jenkins-ci"
description = "User self-service account management app"
version = "2.5"


repositories {
    mavenCentral()
    maven("https://repo.jenkins-ci.org/public/")
    maven("https://git.captcha.com/maven.git/blob_plain/HEAD:/")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

plugins.withId("java") {
    the<JavaPluginExtension>().toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    compileOnly("org.glassfish:javax.json:1.1.4")
    compileOnly("commons-codec:commons-codec:1.15")

    compileOnly("org.kohsuke.stapler:stapler:1.263")
    compileOnly("org.kohsuke.stapler:stapler-jelly:1.263")
    compileOnly("org.kohsuke.stapler:stapler-openid-server:[1.0,2.0)")

    compileOnly("commons-jelly:commons-jelly-tags-define:1.0")

    compileOnly("javax.mail:javax.mail-api:1.6.2")
    compileOnly("javax.activation:activation:1.1.1")

    compileOnly("io.jenkins.backend:jira-rest-ldap-syncer:1.2") {
        exclude(module ="javamail")
    }

    compileOnly("org.webjars:webjars-servlet-2.x:1.6")
    compileOnly("org.webjars:jquery:3.6.1")
    compileOnly("org.webjars:jquery-ui:1.13.2")
    compileOnly("org.webjars.bower:fontawesome:4.7.0")

    compileOnly("com.captcha:botdetect-jsp20:4.0.beta3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    named("war") {
        dependsOn("check")
    }
}

gretty {
    contextPath = "/account"
    httpPort = 8080
}
