pipeline {
    agent any
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Build and upload to artifactory') {
            steps {
                sh 'mvn deploy -s jfrog-settings.xml'
            }
        }
    }
}