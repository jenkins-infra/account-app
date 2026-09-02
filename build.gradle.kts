plugins {
    java
    `jvm-test-suite`
    `maven-publish`
    war
    id("org.gretty") version "4.1.10"
    id("com.github.ben-manes.versions") version "0.53.0"
}

group = "org.jenkins-ci"
description = "User self-service account management app"
version = "2.5"

repositories {
    mavenCentral()
    maven("https://repo.jenkins-ci.org/public/")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

plugins.withId("java") {
    the<JavaPluginExtension>().toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("integrationTest") {
            sources {
                java {
                    setSrcDirs(listOf("src/it/java"))
                }
            }

            dependencies {
                implementation(project())

                implementation("io.github.bonigarcia:webdrivermanager:6.3.3")

                implementation("com.sun.mail:jakarta.mail:2.0.2")

                implementation("org.seleniumhq.selenium:selenium-java:4.43.0")
                implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.43.0")
                implementation("org.assertj:assertj-core:3.27.7")

                implementation("com.unboundid:unboundid-ldapsdk:7.0.5")

                implementation("com.icegreen:greenmail-junit5:2.1.10")
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter("test")
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

dependencies {
    implementation("com.typesafe:config:1.4.5")

    implementation("commons-codec:commons-codec:1.20.0")
    implementation("org.apache.commons:commons-lang3:3.17.0")

    implementation("org.kohsuke.stapler:stapler-jelly:2106.2111.v22866cc60465")

    implementation("commons-jelly:commons-jelly-tags-define:1.0")

    implementation("com.sun.mail:jakarta.mail:2.0.2")

    implementation("com.sun.activation:jakarta.activation:2.0.1")

    implementation("com.github.cage:cage:1.0")

    implementation("com.github.spotbugs:spotbugs-annotations:4.9.8")

    implementation("com.google.guava:guava:33.5.0-jre")

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.2")
}

tasks {
    test {
        useJUnitPlatform()
        failOnNoDiscoveredTests = false
    }
    withType<org.akhikhl.gretty.AppBeforeIntegrationTestTask> {
        doFirst {
            jvmArgs = listOf("-DSMTP_PORT=3025", "-DLDAP_URL=ldap://localhost:3389")
        }
    }
    named("war") {
        dependsOn("check")
    }
}

gretty {
    contextPath = "/"
    httpPort = 8080

    integrationTestTask = "integrationTest"
    servletContainer = "jetty11"
}
