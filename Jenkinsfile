pipeline {
    agent any

    tools {
        maven 'Maven_3'
        jdk 'JDK_17'
    }

    environment {
        DEPLOYMENT_PATH="C:\\Program Files\\Apache Software Foundation\\Tomcat 10.1\\webapps"
        LOG_PATH= "C:\\logs\\cicd-pipeline"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', credentialsId: 'cicd_token', url:  'https://github.com/dharam-java/cicd-pipeline.git'
            }
        }

        stage('Build WAR') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Deploy WAR') {
            steps {
                bat '''
                    copy /Y target\\*.war "%DEPLOYMENT_PATH%\\"
                '''
            }
        }
    }
}