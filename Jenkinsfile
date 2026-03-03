pipeline {
    agent { label 'Gubkovskiy_agent' }

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
        string(
            name: 'DOCKER_IMAGE',
            defaultValue: 'thearity/restobot-app',
            description: 'Target image repository (e.g. Docker Hub repo)')
        string(
            name: 'DOCKER_TAG',
            defaultValue: 'latest',
            description: 'Image tag (e.g. latest, 1.0.${BUILD_NUMBER})')
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        DOCKER_CREDENTIALS_ID = 'dockerhub-restobot' // Jenkins credentials id (username/password)
        JAVA_HOME = "/usr/lib/jvm/temurin-23-jdk-amd64" 
        PATH = "${JAVA_HOME}/bin:${PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare .env') {
            steps {
                withCredentials([file(credentialsId: 'restobot_env', variable: 'ENV_FILE')]) {
                    sh 'cp "$ENV_FILE" .env'
                }
            }
        }

        stage('Run PostgreSQL') {
            steps {
                sh 'docker compose up -d postgres'
            }
        }

        stage('Run Migrations') {
            steps {
                sh '''
psql -h localhost -U postgres -p 5435 -d main -f logic/src/main/resources/db/migration/main/V1__init_main.sql
psql -h localhost -U postgres -p 5435 -d main -f logic/src/main/resources/db/migration/main/V2__add_data.sql
'''
            }
        }

        stage('Start Application') {
            steps {
                sh 'docker compose up -d'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
