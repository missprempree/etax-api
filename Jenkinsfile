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

	stage("Docker Build") {
            steps {
               sh '''
                   #oc start-build --from-build=<build_name>
                   oc start-build -F etax --from-dir=.
               '''
            }
        }
		
        stage("Docker Login") {
            steps {
                script {
                    // Use environment variables for credentials
                    sh '''
                        echo dckr_pat_2Rh4dvtW17yXDbN7rhQ5OEJZ_9U | docker login -u delenies --password-stdin
                    '''
                }
            }
        }
	    
        stage("Docker Push") {
            steps {
                script {
                    sh '''
                        docker tag etax:latest delenies/etax:latest
                        docker push delenies/etax:latest
                    '''
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
}
