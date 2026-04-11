pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        RAW_ENV_PATH = "${WORKSPACE}/.env.k8s"
        ENV_PATH = "${WORKSPACE}/.env"
        K8S_NAMESPACE = 'restobot'
        LOCAL_POSTGRES_CONTAINER = 'restobot-ci-postgres'
        LOCAL_POSTGRES_PORT = '55432'
        IMAGE_NAME = 'restobot-app'
        MINIKUBE_PROFILE = 'minikube'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare environment') {
            steps {
                withCredentials([file(credentialsId: 'restobot_env', variable: 'ENV_FILE')]) {
                    sh '''set -eu
                    perl -pe 's/\r$//' "$ENV_FILE" > "$RAW_ENV_PATH"
                    grep -v '^MAIN_DB_URL=' "$RAW_ENV_PATH" > "$ENV_PATH" || true
                    printf 'MAIN_DB_URL=jdbc:postgresql://127.0.0.1:%s/main\n' "$LOCAL_POSTGRES_PORT" >> "$ENV_PATH"
                    '''
                }
            }
        }

        stage('Verify tools') {
            steps {
                sh '''set -eu
                java -version
                docker version --format '{{.Server.Version}}'
                kubectl version --client
                minikube -p "$MINIKUBE_PROFILE" status
                kubectl config use-context "$MINIKUBE_PROFILE"
                '''
            }
        }

        stage('Build artifact') {
            steps {
                sh '''set -eu
                chmod +x gradlew
                . "$ENV_PATH"

                docker rm -f "$LOCAL_POSTGRES_CONTAINER" >/dev/null 2>&1 || true
                docker run -d \
                  --name "$LOCAL_POSTGRES_CONTAINER" \
                  -e POSTGRES_USER="$MAIN_DB_USER" \
                  -e POSTGRES_PASSWORD="$MAIN_DB_PASSWORD" \
                  -e POSTGRES_DB=postgres \
                  -p "$LOCAL_POSTGRES_PORT":5432 \
                  postgres:16-alpine

                until docker exec "$LOCAL_POSTGRES_CONTAINER" pg_isready -U "$MAIN_DB_USER" -d postgres >/dev/null 2>&1; do
                  echo "Waiting for local PostgreSQL..."
                  sleep 3
                done

                if ! docker exec -e PGPASSWORD="$MAIN_DB_PASSWORD" "$LOCAL_POSTGRES_CONTAINER" \
                  psql -U "$MAIN_DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'main'" | grep -q 1; then
                  docker exec -e PGPASSWORD="$MAIN_DB_PASSWORD" "$LOCAL_POSTGRES_CONTAINER" \
                    psql -U "$MAIN_DB_USER" -d postgres -c "CREATE DATABASE main"
                fi

                ./gradlew clean :logic:flywayMigrate :logic:generateJooq build :app:shadowJar
                '''
            }
        }

        stage('Build Docker image') {
            steps {
                script {
                    env.IMAGE = "${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                }
                sh '''set -eu
                docker build -t "$IMAGE" .
                '''
            }
        }

        stage('Deploy to minikube') {
            steps {
                sh '''set -eu
                kubectl config use-context "$MINIKUBE_PROFILE"

                kubectl apply -f k8s/namespace.yaml

                kubectl -n "$K8S_NAMESPACE" create secret generic restobot-app-env \
                  --from-env-file="$RAW_ENV_PATH" \
                  --dry-run=client -o yaml | kubectl apply -f -

                kubectl -n "$K8S_NAMESPACE" create configmap restobot-db-migrations \
                  --from-file=logic/src/main/resources/db/migration/main \
                  --dry-run=client -o yaml | kubectl apply -f -

                kubectl apply -f k8s/postgres.yaml
                kubectl -n "$K8S_NAMESPACE" rollout status deployment/restobot-postgres --timeout=180s

                minikube -p "$MINIKUBE_PROFILE" image load "$IMAGE"

                kubectl -n "$K8S_NAMESPACE" delete job restobot-db-migrate --ignore-not-found=true
                kubectl apply -f k8s/migration-job.yaml
                kubectl -n "$K8S_NAMESPACE" wait --for=condition=complete job/restobot-db-migrate --timeout=180s

                kubectl apply -f k8s/app.yaml
                kubectl -n "$K8S_NAMESPACE" set image deployment/restobot-app restobot-app="$IMAGE"
                kubectl -n "$K8S_NAMESPACE" rollout status deployment/restobot-app --timeout=180s
                kubectl -n "$K8S_NAMESPACE" get pods,svc
                '''
            }
        }
    }

    post {
        failure {
            sh '''set +e
            kubectl -n "$K8S_NAMESPACE" get pods
            kubectl -n "$K8S_NAMESPACE" logs job/restobot-db-migrate
            '''
        }

        always {
            sh '''set +e
            docker rm -f "$LOCAL_POSTGRES_CONTAINER" >/dev/null 2>&1 || true
            '''
            deleteDir()
        }
    }
}
