pipeline {
    environment {
        JAVA_HOME = '/opt/jdk-17'
    }
    agent {
        // infra.ci build on arm64 in the Dockerfile, as it's used in production
        label 'jdk17 || linux-arm64-docker'
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
                    automaticSemanticVersioning: true,
                    targetplatforms: 'linux/amd64,linux/arm64',
                    disablePublication: !infra.isInfra()
                ])
            }
        }
    }
}
