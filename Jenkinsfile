pipeline {
    agent {
        label: 'jdk11'
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
                junit 'build/reports/tests/*.xml'
              }
            }
        }
    }
}
