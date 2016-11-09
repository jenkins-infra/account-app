#!/usr/bin/env groovy

def imageName = 'jenkinsciinfra/account-app'

/* Only keep the X most recent builds. */
properties([[$class: 'jenkins.model.BuildDiscarderProperty',
            strategy: [$class: 'LogRotator',
                        numToKeepStr: '10',
                        artifactNumToKeepStr: '10']]])

node('docker') {
    stage('Build') {
        timestamps {
            checkout scm
            docker.image('java:8').inside {
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
            timestamps { container.push() }
        }
    }
}
