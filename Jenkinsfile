pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    stages {
        stage('Build and archive') {
            agent any

            environment {
                GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
                ENV_PATH = "${WORKSPACE}/.env"
                DB_HOST = "127.0.0.1"
                DB_PORT = "5435"
                DB_NAME = "main"
                JAVA_TOOL_OPTIONS = "-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true"
                GRADLE_OPTS = "-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true"
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
                                printf '\nMAIN_DB_URL=jdbc:postgresql://%s:%s/%s\n' "$DB_HOST" "$DB_PORT" "$DB_NAME" >> "$ENV_PATH"
                            fi
                            ls -l "$ENV_PATH"
                            '''
                        }
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

                stage('Start DB') {
                    steps {
                        sh '''#!/usr/bin/env bash
                        set -euo pipefail

                        compose() {
                            if docker compose version >/dev/null 2>&1; then
                                docker compose "$@"
                            elif command -v docker-compose >/dev/null 2>&1; then
                                docker-compose "$@"
                            else
                                echo "ERROR: Docker Compose is required to start PostgreSQL for this build." >&2
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
                        for _ in $(seq 1 30); do
                            if psql -h "$DB_HOST" -U "$MAIN_DB_USER" -p "$DB_PORT" -d postgres -tAc 'SELECT 1' >/dev/null 2>&1; then
                                exit 0
                            fi
                            sleep 2
                        done

                        echo "ERROR: PostgreSQL did not become ready in time." >&2
                        compose logs postgres
                        exit 1
                        '''
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
                        psql -h "$DB_HOST" -U "$MAIN_DB_USER" -p "$DB_PORT" -d postgres <<'SQL'
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

                stage('Verify JVM DB Socket') {
                    steps {
                        sh '''#!/usr/bin/env bash
                        set -euo pipefail
                        cat > /tmp/JenkinsDbSocketCheck.java <<'JAVA'
import java.net.InetSocketAddress;
import java.net.Socket;

public class JenkinsDbSocketCheck {
    public static void main(String[] args) throws Exception {
        String host = System.getenv("DB_HOST");
        int port = Integer.parseInt(System.getenv("DB_PORT"));
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 5000);
            System.out.println("JVM can connect to " + host + ":" + port);
        }
    }
}
JAVA
                        javac /tmp/JenkinsDbSocketCheck.java
                        java -cp /tmp JenkinsDbSocketCheck
                        '''
                    }
                }

                stage('Run Migrations') {
                    steps {
                        sh './gradlew --no-daemon --stacktrace --info :logic:flywayMigrate'
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
                    sh '''#!/usr/bin/env bash
                    set +e
                    if [ -f docker-compose.yml ] && [ -f "$ENV_PATH" ]; then
                        if docker compose version >/dev/null 2>&1; then
                            docker compose --env-file "$ENV_PATH" down -v --remove-orphans
                        elif command -v docker-compose >/dev/null 2>&1; then
                            docker-compose --env-file "$ENV_PATH" down -v --remove-orphans
                        fi
                    fi
                    '''
                    deleteDir()
                }
            }
        }
    }
}
