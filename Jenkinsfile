pipeline {
    agent any
	
    stages {
	   stage('Checkout') {
            steps {
                git url: 'https://github.com/missprempree/etax-api', branch: 'master'
            }
        }
		
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

		
	stage('Docker Build') {
            steps {
                binaryBuild(buildConfigName: 'etax', buildFromPath: '.')
            }
        }

		
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
