pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        ENV_PATH = "${WORKSPACE}/.env"
        DB_HOST = "localhost"
        DB_PORT = "5432"
        DB_NAME = "main"
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

                    if grep -q '^MAIN_DB_URL=' "$ENV_PATH"; then
                        sed "s#^MAIN_DB_URL=.*#MAIN_DB_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}#" "$ENV_PATH" > "${ENV_PATH}.tmp"
                        mv "${ENV_PATH}.tmp" "$ENV_PATH"
                    else
                        printf '\\nMAIN_DB_URL=jdbc:postgresql://%s:%s/%s\\n' "$DB_HOST" "$DB_PORT" "$DB_NAME" >> "$ENV_PATH"
                    fi

                    ls -l "$ENV_PATH"
                    '''
                }
            }
        }

        stage('Check Local DB') {
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail

                set -a
                . "$ENV_PATH"
                set +a
                export PGPASSWORD="$MAIN_DB_PASSWORD"

                echo "Checking local PostgreSQL on ${DB_HOST}:${DB_PORT}..."
                for _ in $(seq 1 15); do
                    if psql -h "$DB_HOST" -U "$MAIN_DB_USER" -p "$DB_PORT" -d postgres -tAc 'SELECT 1' >/dev/null 2>&1; then
                        exit 0
                    fi
                    sleep 2
                done

                echo "ERROR: Local PostgreSQL is not available on ${DB_HOST}:${DB_PORT}." >&2
                echo "Start local PostgreSQL and make sure MAIN_DB_USER can connect to the postgres database." >&2
                exit 1
                '''
            }
        }

        stage('Create DB') {
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail

                set -a
                . "$ENV_PATH"
                set +a
                export PGPASSWORD="$MAIN_DB_PASSWORD"

                if psql -h "$DB_HOST" -U "$MAIN_DB_USER" -p "$DB_PORT" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '${DB_NAME}'" | grep -qx '1'; then
                    echo "Database ${DB_NAME} already exists."
                else
                    psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -U "$MAIN_DB_USER" -p "$DB_PORT" -d postgres \
                        -c "CREATE DATABASE ${DB_NAME};"
                fi
                '''
            }
        }

        stage('Verify DB Connection') {
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail

                set -a
                . "$ENV_PATH"
                set +a
                export PGPASSWORD="$MAIN_DB_PASSWORD"

                echo "MAIN_DB_URL=$MAIN_DB_URL"
                psql -h "$DB_HOST" -U "$MAIN_DB_USER" -p "$DB_PORT" -d "$DB_NAME" -tAc 'SELECT 1'
                '''
            }
        }

        stage('Resolve Java') {
            steps {
                timeout(time: 15, unit: 'SECONDS') {
                    script {
                        env.JAVA_HOME = sh(
                            script: '''#!/usr/bin/env bash
                            set -euo pipefail

                            is_jdk_home() {
                                [ -n "$1" ] && [ -x "$1/bin/java" ] && [ -x "$1/bin/javac" ]
                            }

                            if is_jdk_home "${JAVA_HOME:-}"; then
                                printf '%s\\n' "$JAVA_HOME"
                                exit 0
                            fi

                            for candidate in \
                                /Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home \
                                /Library/Java/JavaVirtualMachines/temurin-23.jdk/Contents/Home \
                                /usr/lib/jvm/temurin-23-jdk-amd64 \
                                /usr/lib/jvm/java-23-openjdk-amd64
                            do
                                if is_jdk_home "$candidate"; then
                                    printf '%s\\n' "$candidate"
                                    exit 0
                                fi
                            done

                            if command -v /usr/libexec/java_home >/dev/null 2>&1; then
                                candidate="$(/usr/libexec/java_home -v 23 2>/dev/null || true)"
                                if is_jdk_home "$candidate"; then
                                    printf '%s\\n' "$candidate"
                                    exit 0
                                fi
                            fi

                            if command -v java >/dev/null 2>&1; then
                                candidate="$(java -XshowSettings:properties -version 2>&1 | awk -F= '/^[[:space:]]*java.home =/ { gsub(/^[[:space:]]+|[[:space:]]+$/, "", $2); print $2; exit }')"
                                if is_jdk_home "$candidate"; then
                                    printf '%s\\n' "$candidate"
                                    exit 0
                                fi
                            fi

                            echo 'ERROR: Java 23 JDK was not found on this Jenkins node.' >&2
                            exit 1
                            ''',
                            returnStdout: true
                        ).trim()
                        env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
                    }

                    sh '''#!/usr/bin/env bash
                    set -euo pipefail

                    echo "JAVA_HOME=$JAVA_HOME"
                    java -version
                    javac -version
                    '''
                }
            }
        }

        stage('Gradle Prep') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Run Migrations') {
            steps {
                sh './gradlew --no-daemon --stacktrace :logic:flywayMigrate'
            }
        }

        stage('Generate jOOQ') {
            steps {
                sh './gradlew --no-daemon :logic:generateJooq'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew --no-daemon build'
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
