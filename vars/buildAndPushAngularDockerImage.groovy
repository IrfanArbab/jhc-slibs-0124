// vars/buildAndPushAngularDockerImage.groovy

def call(Map config) {
    pipeline {
        agent any
        parameters {
            string(name: 'IMAGE_TAG', defaultValue: '0.1', description: 'Docker image tag')
            string(name: 'ECR_REPO_URL', defaultValue: '891376912626.dkr.ecr.us-east-1.amazonaws.com/angular-ecr', description: 'ECR repository URL')
        }
        
        stages {
            stage('Build Angular Application') {
                steps {
                    script {
                        // Install dependencies and build Angular app
                        bat 'npm install'
                        bat 'npm run build --prod'
                    }
                }
            }
            
            stage('Build Docker Image') {
                steps {
                    script {
                        // Build the Docker image
                        docker.build("${config.IMAGE_TAG}", '.')
                    }
                }
            }
            
            stage('Run Docker Image') {
                steps {
                    script {
                        // Run the Docker image as a container
                        docker.image("${config.IMAGE_TAG}").run('-p 4200:4200 -d')
                    }
                }
            }
            
            stage('Push Docker Image to ECR') {
                steps {
                    script {
                        // Log in to ECR and push the Docker image
                        docker.withRegistry('https://' + config.ECR_REPO_URL, 'ecr:us-east-1') {
                            docker.image("${config.IMAGE_TAG}").push("${config.ECR_REPO_URL}:${config.IMAGE_TAG}")
                        }
                    }
                }
            }
        }
    }
}
