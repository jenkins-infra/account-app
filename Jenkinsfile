pipeline {
    agent {
        label 'jdk11'
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
            }
        }
    }
}
