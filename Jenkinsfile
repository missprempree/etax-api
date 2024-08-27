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
                sh '''
                   oc start-build -F etax --from-dir=.
               '''
            }
        }

	stage("Run Ansible Job Template"){
            steps {
                ansibleTower jobTemplate: 'yatphiroon-etax-api-job', 
                            jobType: 'run', 
                            throwExceptionWhenFail: false, 
                            towerCredentialsId: 'ansible', 
                            towerLogLevel: 'false', 
                            towerServer: 'cd-ansible'
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
