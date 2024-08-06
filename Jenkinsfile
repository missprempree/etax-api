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

        stage('Get Version from POM') {
            steps {
                script {
                    // Use Maven to read the version from the pom.xml file
                    VERSION = sh(script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout', returnStdout: true).trim()
                    echo "Version: ${VERSION}"
                }
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
