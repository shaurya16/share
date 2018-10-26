pipeline {
    agent any
    tools {
        maven 'Maven'
        jdk 'JDK 8'
    }
    stages {
       stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
       }
       stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
    }
}
