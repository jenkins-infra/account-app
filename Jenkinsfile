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
    }
}
