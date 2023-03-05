plugins {
    java
    `jvm-test-suite`
    `maven-publish`
    war
    id("org.gretty") version "3.1.0"
    id("com.github.ben-manes.versions") version "0.46.0"
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
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

plugins.withId("java") {
    the<JavaPluginExtension>().toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

testing {
    suites {
        @Suppress("UnstableApiUsage") val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        @Suppress("UnstableApiUsage", "UNUSED_VARIABLE") val integrationTest by registering(JvmTestSuite::class) {
            sources {
                java {
                    setSrcDirs(listOf("src/it/java"))
                }
            }

            dependencies {
                implementation(project())

                implementation("io.github.bonigarcia:webdrivermanager:5.3.2")

                implementation("com.sun.mail:jakarta.mail:2.0.1")

                implementation("org.seleniumhq.selenium:selenium-java:4.8.1")
                implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.8.1")
                implementation("org.assertj:assertj-core:3.24.2")

                implementation("com.unboundid:unboundid-ldapsdk:6.0.7")

                implementation("com.icegreen:greenmail-junit5:2.0.0")
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }

            testType.set(TestSuiteType.INTEGRATION_TEST)
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

dependencies {
    implementation("com.typesafe:config:1.4.2")

    implementation("javax.servlet:javax.servlet-api:4.0.1")

    implementation("org.glassfish:javax.json:1.1.4")
    implementation("commons-codec:commons-codec:1.15")

    implementation("org.kohsuke.stapler:stapler-jelly:1747.v9580b_2f1d80b")
    implementation("org.kohsuke.stapler:stapler-openid-server:1.0")

    implementation("commons-jelly:commons-jelly-tags-define:1.0")

    implementation("com.sun.mail:jakarta.mail:2.0.1")

    implementation("com.sun.activation:jakarta.activation:2.0.1")

    implementation("org.webjars:webjars-servlet-2.x:1.6")
    implementation("org.webjars:jquery:3.6.3")
    implementation("org.webjars:jquery-ui:1.13.2")
    implementation("org.webjars.bower:fontawesome:4.7.0")

    implementation("com.captcha:botdetect-jsp20:4.0.beta3.7")

    implementation("com.github.spotbugs:spotbugs-annotations:4.7.3")

    implementation("com.google.guava:guava:31.1-jre")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
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
    servletContainer = "jetty10"
}
