pipeline {
    agent any
    tools {
        maven 'Maven 4'
    }
    stages {
        stage('Clone Repository') {
            steps {
                git 'https://github.com/missprempree/etax-api.git'
            }
        }
        stage('Build Maven Project') {
            steps {
                sh 'mvn clean package'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    def dockerImage = docker.build("etax-docker-image:${env.BUILD_NUMBER}")
                    dockerImage.push("latest")
                    dockerImage.push("${env.BUILD_NUMBER}")
                }
            }
        }
    }
}
