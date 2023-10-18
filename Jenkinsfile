pipeline {
    agent {
        label 'jdk17'
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
                parallelDockerUpdatecli([imageName: 'account-app', rebuildImageOnPeriodicJob: false, buildDockerConfig: [targetplatforms: 'linux/amd64,linux/arm64']])
            }
        }
    }
}
