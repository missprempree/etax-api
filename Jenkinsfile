library identifier: "pipeline-library@v1.5",
retriever: modernSCM(
  [
    $class: "GitSCMSource",
    remote: "https://github.com/missprempree/etax-api.git"
  ]
)

appName = "etax-api"

pipeline {
    agent { label "maven" }
    stages {
        stage("Checkout") {
            steps {
                checkout scm
            }
        }
        stage("Docker Build") {
            steps {
                binaryBuild(buildConfigName: appName, buildFromPath: ".")
            }
        }

    }
}
