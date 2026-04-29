pipeline {
    agent { label 'labs' }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        ENV_PATH = "${WORKSPACE}/.env"
        JAVA_TOOLCHAIN_VERSION = "21"
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
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    # Normalize line endings to LF to avoid `/bin/sh` parse issues
                    perl -pe 's/\r$//' "$ENV_FILE" > "$ENV_PATH"
                    ls -l "$ENV_PATH"
                    '''
                }
            }
        }

        stage('Resolve Java') {
            steps {
                script {
                    env.JAVA_HOME = sh(
                        script: '''#!/usr/bin/env bash
                        set -euo pipefail
                        if [ -x /usr/lib/jvm/java-21-openjdk-amd64/bin/javac ]; then
                          echo /usr/lib/jvm/java-21-openjdk-amd64
                        else
                          JAVA_BIN="$(command -v javac || command -v java)"
                          dirname "$(dirname "$(readlink -f "$JAVA_BIN")")"
                        fi
                        ''',
                        returnStdout: true
                    ).trim()
                    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
                }

                sh 'java -version'
            }
        }

        stage('Gradle Prep') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Create DB') {
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                ENV_PATH="${ENV_PATH:-${WORKSPACE:-$PWD}/.env}"
                if [ ! -f "$ENV_PATH" ]; then
                  echo "ERROR: $ENV_PATH not found. Make sure the credential 'restobot_env' is configured."
                  exit 1
                fi

                set -a
                . "$ENV_PATH"
                set +a

                export PGPASSWORD="$MAIN_DB_PASSWORD"
                psql -h localhost -U "$MAIN_DB_USER" -p 5435 -d postgres -v ON_ERROR_STOP=1 \
                  -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'main' AND pid <> pg_backend_pid();" \
                  -c "DROP DATABASE IF EXISTS main;" \
                  -c "CREATE DATABASE main;"
                '''
            }
        }

        stage('Run Migrations') {
            steps {
                sh './gradlew -PjavaToolchainVersion="$JAVA_TOOLCHAIN_VERSION" :logic:flywayMigrate'
            }
        }

        stage('Generate jOOQ') {
            steps {
                sh './gradlew -PjavaToolchainVersion="$JAVA_TOOLCHAIN_VERSION" :logic:generateJooq'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew -PjavaToolchainVersion="$JAVA_TOOLCHAIN_VERSION" build'
            }
        }
    }


    post {
        always {
            archiveArtifacts artifacts: 'app/build/libs/*.jar', allowEmptyArchive: true, fingerprint: true
            deleteDir()
        }
    }
}
