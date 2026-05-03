pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        JAVA_HOME = "/usr/bin/java" 
        PATH = "${JAVA_HOME}/bin:${PATH}"
        ENV_PATH = "${WORKSPACE}/.env"
        DB_HOST = "localhost"
        DB_PORT = "5435"
        DB_NAME = "main"
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
                    sh '''set -e
                    # Normalize line endings to LF to avoid `/bin/sh` parse issues
                    perl -pe 's/\r$//' "$ENV_FILE" > "$ENV_PATH"
                    if grep -q '^MAIN_DB_URL=' "$ENV_PATH"; then
                        sed "s#^MAIN_DB_URL=.*#MAIN_DB_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}#" "$ENV_PATH" > "${ENV_PATH}.tmp"
                        mv "${ENV_PATH}.tmp" "$ENV_PATH"
                    else
                        printf '\nMAIN_DB_URL=jdbc:postgresql://%s:%s/%s\n' "$DB_HOST" "$DB_PORT" "$DB_NAME" >> "$ENV_PATH"
                    fi
                    ls -l "$ENV_PATH"
                    '''
                }
            }
        }

        stage('Gradle Prep') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Start DB') {
            steps {
                sh '''set -e
                    ENV_PATH="${ENV_PATH:-${WORKSPACE:-$PWD}/.env}"
                    if [ ! -f "$ENV_PATH" ]; then
                        echo "ERROR: $ENV_PATH not found. Make sure the credential 'restobot_env' is configured."
                        exit 1
                    fi

                    compose() {
                        if docker compose version >/dev/null 2>&1; then
                            docker compose "$@"
                        elif command -v docker-compose >/dev/null 2>&1; then
                            docker-compose "$@"
                        else
                            echo "ERROR: Docker Compose is required to start PostgreSQL for this build."
                            return 127
                        fi
                    }

                    compose --env-file "$ENV_PATH" down -v --remove-orphans
                    compose --env-file "$ENV_PATH" up -d postgres

                    set -a
                    . "$ENV_PATH"
                    set +a
                    export PGPASSWORD="$MAIN_DB_PASSWORD"

                    echo "Waiting for PostgreSQL on ${DB_HOST}:${DB_PORT}..."
                    for i in $(seq 1 30); do
                        if psql -h "$DB_HOST" -U "$MAIN_DB_USER" -p "$DB_PORT" -d postgres -tAc 'SELECT 1' >/dev/null 2>&1; then
                            exit 0
                        fi
                        sleep 2
                    done

                    echo "ERROR: PostgreSQL did not become ready in time."
                    compose logs postgres
                    exit 1
                    '''
            }
        }

        stage('Create DB') {
            steps {
                sh '''set -e
                    ENV_PATH="${ENV_PATH:-${WORKSPACE:-$PWD}/.env}"
                    if [ ! -f "$ENV_PATH" ]; then
                        echo "ERROR: $ENV_PATH not found. Make sure the credential 'restobot_env' is configured."
                        exit 1
                    fi
                    set -a
                    . "$ENV_PATH"
                    set +a
                    export PGPASSWORD="$MAIN_DB_PASSWORD"
                    DB_NAME_LITERAL="$(printf "%s" "$DB_NAME" | sed "s/'/''/g")"
                    DB_NAME_IDENTIFIER="$(printf "%s" "$DB_NAME" | sed 's/"/""/g')"
                    psql -v ON_ERROR_STOP=1 -h "$DB_HOST" -U "$MAIN_DB_USER" -p "$DB_PORT" -d postgres \
                        -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${DB_NAME_LITERAL}' AND pid <> pg_backend_pid();" \
                        -c "DROP DATABASE IF EXISTS \"${DB_NAME_IDENTIFIER}\";" \
                        -c "CREATE DATABASE \"${DB_NAME_IDENTIFIER}\";"
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

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'app/build/libs/app-fat.jar', fingerprint: true
            }
        }
    }


    post {
        always {
            sh '''set +e
                if [ -f docker-compose.yml ]; then
                    if docker compose version >/dev/null 2>&1; then
                        if [ -f "$ENV_PATH" ]; then
                            docker compose --env-file "$ENV_PATH" down -v --remove-orphans
                        else
                            docker compose down -v --remove-orphans
                        fi
                    elif command -v docker-compose >/dev/null 2>&1; then
                        if [ -f "$ENV_PATH" ]; then
                            docker-compose --env-file "$ENV_PATH" down -v --remove-orphans
                        else
                            docker-compose down -v --remove-orphans
                        fi
                    fi
                fi
                '''
            // Always clean workspace to avoid leftover files between builds
            cleanWs()
        }
    }
}
