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

	stage('Java Build') {
            steps {
                // Run Maven build
                sh 'mvn clean install'
            }
        }

	stage('Docker Build') {
	    steps {
	        script {
	            sh 'oc start-build etax --from-dir=.'
	        }
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
