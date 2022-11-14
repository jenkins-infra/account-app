pipeline {
    agent {
        label 'jdk17'
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
