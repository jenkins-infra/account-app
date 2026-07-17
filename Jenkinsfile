pipeline {
    environment {
        JAVA_HOME = '/opt/jdk-17'
    }
    agent {
        // Integration tests requires an x86_64 CPU (Selenium) VM (no sandbox)
        label 'linux-amd64-docker'
    }
    options {
        disableConcurrentBuilds(abortPrevious: true)
    }

    stages {
        stage('Build') {
            steps {
                sh './gradlew build -x test -x integrationTest'
            }
        }
        stage('Test') {
            environment {
                SELENIUM_BROWSER_BINARY = sh(script: 'ls ${HOME}/.cache/ms-playwright/chromium_headless_shell-*/chrome-headless-shell-linux64/chrome-headless-shell', returnStdout: true).trim()
            }
            steps {
                sh './gradlew check'
            }

            post {
              always {
                junit 'build/test-results/**/TEST-*.xml'
              }
              unsuccessful {
                archiveArtifacts allowEmptyArchive: true, artifacts: 'errorScreenshots/*.jpg'
              }
            }
        }
        stage('Docker image') {
            steps {
                buildDockerAndPublishImage('account-app', [
                    rebuildImageOnPeriodicJob: false,
                    publishToPrivateAzureRegistry: true,
                    targetplatforms: 'linux/arm64',
                    disablePublication: !infra.isInfra()
                ])
            }
        }
    }
}
