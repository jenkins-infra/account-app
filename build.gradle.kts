plugins {
    java
    `jvm-test-suite`
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

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        val integrationTest by registering(JvmTestSuite::class) {
            sources {
                java {
                    setSrcDirs(listOf("src/it/java"))
                }
            }

            dependencies {
                implementation(project)

                implementation("io.github.bonigarcia:webdrivermanager:5.3.0")

                implementation("org.seleniumhq.selenium:selenium-java:4.5.3")
                implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.6.0")
                implementation("org.assertj:assertj-core:3.23.1")

                implementation("com.unboundid:unboundid-ldapsdk:6.0.6")
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

    implementation("org.kohsuke.stapler:stapler:1.263")
    implementation("org.kohsuke.stapler:stapler-jelly:1.263")
    implementation("org.kohsuke.stapler:stapler-openid-server:[1.0,2.0)")

    implementation("commons-jelly:commons-jelly-tags-define:1.0")

    implementation("com.sun.mail:jakarta.mail:1.6.7")

    implementation("jakarta.activation:jakarta.activation-api:1.2.2")

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
    contextPath = "/"
    httpPort = 8080

    integrationTestTask = "integrationTest"
}
