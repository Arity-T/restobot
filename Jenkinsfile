pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    parameters {
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: 'Run unit and component tests')
        booleanParam(
            name: 'PUSH_IMAGE',
            defaultValue: false,
            description: 'Push Docker image after build')
        stringParam(
            name: 'DOCKER_IMAGE',
            defaultValue: 'thearity/restobot-app',
            description: 'Target image repository (e.g. Docker Hub repo)')
        stringParam(
            name: 'DOCKER_TAG',
            defaultValue: 'latest',
            description: 'Image tag (e.g. latest, 1.0.${BUILD_NUMBER})')
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        DOCKER_CREDENTIALS_ID = 'dockerhub-restobot' // Jenkins credentials id (username/password)
    }

    tools {
        jdk 'temurin-23' // Configure this JDK name in Jenkins global tools
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prep') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Code Style') {
            steps {
                sh './gradlew spotlessCheck'
            }
        }

        stage('Tests') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Build fat jar') {
            steps {
                sh './gradlew :app:shadowJar'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'app/build/libs/app-fat.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${params.DOCKER_IMAGE}:${params.DOCKER_TAG} ."
            }
        }

        stage('Docker Push') {
            when {
                expression { return params.PUSH_IMAGE }
            }
            steps {
                withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_PASS')]) {
                    sh 'echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin'
                    sh "docker push ${params.DOCKER_IMAGE}:${params.DOCKER_TAG}"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
