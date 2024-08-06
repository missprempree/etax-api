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

	stage('Install Maven') {
            steps {
                script {
                    // Define Maven version
                    def mavenVersion = '4.0.0' // Change to the desired version

                    // Install Maven using yum
                    sh """
                    yum install -y java-17
                    yum install -y maven-$mavenVersion
                    """
                }
            }
        }

	stage('Java Build') {
            steps {
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
