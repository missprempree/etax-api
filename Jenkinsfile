pipeline {
    agent {
        dockerfile {
            filename 'DockerFile' // Path to your DockerFile in the repository
            dir '.' // Directory where the Dockerfile is located
            additionalBuildArgs '--no-cache' // Optional: Use if you want to avoid using cached layers
        }
    }
    stages {
        stage('Clone Repository') {
            steps {
                git 'https://github.com/missprempree/etax-api.git'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    // Build the Docker image using the Dockerfile
                    def dockerImage = docker.build("etax-docker-image:${env.BUILD_NUMBER}")
                    dockerImage.push("latest")
                    dockerImage.push("${env.BUILD_NUMBER}")
                }
            }
        }
    }
}
