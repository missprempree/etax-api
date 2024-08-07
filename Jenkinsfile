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

	stage('Build and Push Image') {
            steps {
                script {
                    // Create BuildConfig
                    sh '''
                    oc apply -f - <<EOF
                    apiVersion: build.openshift.io/v1
                    kind: BuildConfig
                    metadata:
                      labels:
                        app.kubernetes.io/name: etax
                      name: etax
                    spec:
                      output:
                        to:
                          kind: DockerImage
                          name: docker.io/delenies/etax:latest
                      source:
                        type: Binary
                        binary: {}
                      strategy:
                        type: Docker
                        dockerStrategy:
                          dockerfilePath: Dockerfile
                    EOF
                    '''

                    // Start the build
                    sh 'oc start-build etax --from-dir=. --follow'
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
