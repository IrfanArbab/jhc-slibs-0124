def call(Map pipelineParams) {
    pipelineParams = pipelineParams ?: [:] // Default to empty map if no params provided

    pipeline {
        agent any

        environment {
            AWS_ACCOUNT_ID = pipelineParams.AWS_ACCOUNT_ID ?: "673106799202"
            AWS_DEFAULT_REGION = pipelineParams.AWS_DEFAULT_REGION ?: "us-east-1"
            IMAGE_REPO_NAME = pipelineParams.IMAGE_REPO_NAME ?: "angular-frontend-home"
            IMAGE_TAG = pipelineParams.IMAGE_TAG ?: "latest"
            REPOSITORY_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}"
        }

        stages {
            stage('Logging into AWS ECR') {
                steps {
                    script {
                        sh "aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"
                    }
                }
            }

            stage('Cloning Git') {
                steps {
                    checkout([$class: 'GitSCM', branches: [[name: '*/main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '', url: 'https://github.com/IrfanArbab/java-backend.git']]])
                }
            }

            stage('Building image') {
                steps {
                    script {
                        dockerImage = docker.build "${IMAGE_REPO_NAME}:${IMAGE_TAG}"
                    }
                }
            }

            stage('Pushing to ECR') {
                steps {
                    script {
                        sh "docker tag ${IMAGE_REPO_NAME}:${IMAGE_TAG} ${REPOSITORY_URI}:${IMAGE_TAG}"
                        sh "docker push ${REPOSITORY_URI}:${IMAGE_TAG}"
                    }
                }
            }
        }
    }
}
