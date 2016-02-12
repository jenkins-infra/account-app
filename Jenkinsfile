#!groovy

/* Only keep the X most recent builds. */
properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator',
                                                          numToKeepStr: '50',
                                                          artifactNumToKeepStr: '20']]])

stage 'Build'
node('docker') {
    checkout scm
    withMavenEnv {
        sh 'mvn clean package'
        step([$class: 'ArtifactArchiver', artifacts: '**/target/*.war', fingerprint: true])
    }

}


/* This code shame-lessly copied and pasted from some Jenkinsfile code abayer
   wrote for the jenkinsci/jenkins project */
void withMavenEnv(List envVars = [], def body) {
    // The names here are currently hardcoded for my test environment. This needs
    // to be made more flexible.
    // Using the "tool" Workflow call automatically installs those tools on the
    // node.
    String mvntool = tool name: "mvn3.3.3", type: 'hudson.tasks.Maven$MavenInstallation'
    String jdktool = tool name: "jdk7_80", type: 'hudson.model.JDK'

    // Set JAVA_HOME, MAVEN_HOME and special PATH variables for the tools we're
    // using.
    List mvnEnv = ["PATH+MVN=${mvntool}/bin", "PATH+JDK=${jdktool}/bin", "JAVA_HOME=${jdktool}", "MAVEN_HOME=${mvntool}"]

    // Add any additional environment variables.
    mvnEnv.addAll(envVars)

    // Invoke the body closure we're passed within the environment we've created.
    withEnv(mvnEnv) {
        body.call()
    }
}
