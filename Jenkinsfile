pipeline {
    agent none

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    stages {

        stage('Build and archive on labs') {
            agent { label 'labs' }

            environment {
                GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
                ENV_PATH = "${WORKSPACE}/.env"
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
                            sh '''#!/usr/bin/env bash
                            set -euo pipefail
                            perl -pe 's/\\r$//' "$ENV_FILE" > "$ENV_PATH"
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
                                JAVA_BIN="$(command -v javac || command -v java)"
                                dirname "$(dirname "$(readlink -f "$JAVA_BIN")")"
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
                    archiveArtifacts artifacts: 'app/build/libs/*.jar', allowEmptyArchive: true, fingerprint: true
                    deleteDir()
                }
            }
        }
    }
}