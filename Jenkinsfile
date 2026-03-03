pipeline {
    agent { label 'Gubkovskiy_agent' }

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        JAVA_HOME = "/usr/lib/jvm/temurin-23-jdk-amd64" 
        PATH = "${JAVA_HOME}/bin:${PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                // Get the latest code from the repository
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

        stage('Gradle Prep') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Create DB') {
            steps {
                sh '''set -e
                    ENV_PATH="${WORKSPACE:-$PWD}/.env"
                    if [ ! -f "$ENV_PATH" ]; then
                    echo "ERROR: $ENV_PATH not found. Make sure the credential 'restobot_env' is configured."
                    exit 1
                    fi
                    set -a
                    . "$ENV_PATH"
                    set +a
                    export PGPASSWORD="$MAIN_DB_PASSWORD"
                    psql -h localhost -U "$MAIN_DB_USER" -p 5435 -d postgres <<'SQL'

                    SELECT pg_terminate_backend(pid)
                    FROM pg_stat_activity
                    WHERE datname = 'main'
                    AND pid <> pg_backend_pid();

                    DROP DATABASE IF EXISTS main;
                    
                    CREATE DATABASE main;
                    SQL
                    '''
            }
        }

        stage('Run Migrations') {
            steps {
                sh './gradlew :logic:flywayMigrate'
            }
        }

        stage('Generate jOOQ') {
            steps {
                sh './gradlew :logic:generateJooq'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }
    }


    post {
        always {
            // Always clean workspace to avoid leftover files between builds
            cleanWs()
        }
    }
}
