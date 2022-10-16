pipeline {
    agent {
        label 'jdk11'
    }

    stages {
        stage('Build') {
            steps {
                sh './gradlew build -x test'
            }
        }
       stage('Test') {
            steps {
                sh './gradlew check'
            }
            post {
              always {
                // enable when tests are added junit 'build/reports/tests/*.xml'
              }
            }
        }
    }
}
