#!groovy

def imageName = 'jenkinsciinfra/account-app'

/* Only keep the X most recent builds. */
properties([[$class: 'jenkins.model.BuildDiscarderProperty',
            strategy: [$class: 'LogRotator',
                        numToKeepStr: '50',
                        artifactNumToKeepStr: '20']]])

stage 'Build'
node('docker') {
    checkout scm
    docker.image('java:8').inside {
        sh './gradlew --no-daemon --info war'
        step([$class: 'ArtifactArchiver',
                artifacts: 'build/libs/*.war',
                fingerprint: true])
    }
}

node('docker') {
    stage 'Package'
    unarchive mapping: ['build/' : 'build/']
    sh 'ls ; ls build; ls build/libs'

    sh 'git rev-parse HEAD > GIT_COMMIT'
    shortCommit = readFile('GIT_COMMIT').take(6)
    def imageTag = "build${shortCommit}"

    echo "Creating the container ${imageName}:${imageTag}"
    def whale = docker.build("${imageName}:${imageTag}")
    stage 'Deploy'
    echo "Shipping ${imageTag}"
    whale.push()
}
