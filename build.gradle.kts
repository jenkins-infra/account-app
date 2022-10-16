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
    implementation("javax.servlet:javax.servlet-api:4.0.1")

    implementation("org.glassfish:javax.json:1.1.4")
    implementation("commons-codec:commons-codec:1.15")

    implementation("org.kohsuke.stapler:stapler:1.263")
    implementation("org.kohsuke.stapler:stapler-jelly:1.263")
    implementation("org.kohsuke.stapler:stapler-openid-server:[1.0,2.0)")

    implementation("commons-jelly:commons-jelly-tags-define:1.0")

    implementation("javax.mail:javax.mail-api:1.6.2")
    implementation("javax.activation:activation:1.1.1")

    implementation("io.jenkins.backend:jira-rest-ldap-syncer:1.2") {
        exclude(module ="javamail")
    }

    implementation("org.webjars:webjars-servlet-2.x:1.6")
    implementation("org.webjars:jquery:3.6.1")
    implementation("org.webjars:jquery-ui:1.13.2")
    implementation("org.webjars.bower:fontawesome:4.7.0")

    implementation("com.captcha:botdetect-jsp20:4.0.beta3")

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
