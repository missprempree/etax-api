pipeline {
    agent { label 'maven' }
    stages {
        stage('Clone Repository') {
            steps {
                git 'https://github.com/missprempree/etax-api.git'
            }
        }
        stage("Docker Build") {
            steps {
                binaryBuild(buildConfigName: 'etax', buildFromPath: ".")
            }
        }
    }
}
