#!/usr/bin/env groovy

def imageName = 'jenkinsciinfra/account-app'

properties([
    buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5')),
    pipelineTriggers([[$class:"SCMTrigger", scmpoll_spec:"H/15 * * * *"]]),
])

node('docker&&linux') {
    stage('Build') {
        timestamps {
            checkout scm
            docker.image('openjdk:8-jdk').inside {
                sh './gradlew --no-daemon --info war'
                archiveArtifacts artifacts: 'build/libs/*.war', fingerprint: true
            }
        }
    }

    def container
    stage('Prepare Container') {
        timestamps {
            sh 'git rev-parse HEAD > GIT_COMMIT'
            shortCommit = readFile('GIT_COMMIT').take(6)
            def imageTag = "${env.BUILD_ID}-build${shortCommit}"
            echo "Creating the container ${imageName}:${imageTag}"
            container = docker.build("${imageName}:${imageTag}")
        }
    }

    /* Assuming we're not inside of a pull request or multibranch pipeline */
    if (!(env.CHANGE_ID || env.BRANCH_NAME)) {
        stage('Publish container') {
            infra.withDockerCredentials {
                timestamps { container.push() }
            }
        }
    }
}
