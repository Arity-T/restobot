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
                name: 'DESTROY_DEPLOYMENT',
                defaultValue: false,
                description: 'Delete all resources deployed by this pipeline in minikube instead of building and deploying')
        string(
                name: 'CI_DB_HOST',
                defaultValue: '127.0.0.1',
                description: 'Hostname used by Gradle/Flyway to connect to the temporary CI PostgreSQL container')
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
        MINIKUBE_DRIVER = 'docker'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare environment') {
            when {
                expression { !params.DESTROY_DEPLOYMENT }
            }
            steps {
                withCredentials([file(credentialsId: 'restobot_env', variable: 'ENV_FILE')]) {
                    sh '''set -eu
                    perl -pe 's/\r$//' "$ENV_FILE" > "$RAW_ENV_PATH"
                    grep -v '^MAIN_DB_URL=' "$RAW_ENV_PATH" > "$ENV_PATH" || true
                    printf 'MAIN_DB_URL=jdbc:postgresql://%s:%s/main\n' "$CI_DB_HOST" "$LOCAL_POSTGRES_PORT" >> "$ENV_PATH"
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

                if [ "${DESTROY_DEPLOYMENT}" = "true" ]; then
                  if minikube -p "$MINIKUBE_PROFILE" status >/dev/null 2>&1; then
                    minikube -p "$MINIKUBE_PROFILE" status
                    kubectl config use-context "$MINIKUBE_PROFILE"
                  else
                    echo "Minikube profile '$MINIKUBE_PROFILE' not found. Cleanup stage will treat it as nothing to delete."
                  fi
                else
                  if ! minikube -p "$MINIKUBE_PROFILE" status >/dev/null 2>&1; then
                    echo "Minikube profile '$MINIKUBE_PROFILE' is missing or stopped. Starting it..."
                    minikube start -p "$MINIKUBE_PROFILE" --driver="$MINIKUBE_DRIVER"
                  fi

                  minikube -p "$MINIKUBE_PROFILE" status
                  kubectl config use-context "$MINIKUBE_PROFILE"
                fi
                '''
            }
        }

        stage('Build artifact') {
            when {
                expression { !params.DESTROY_DEPLOYMENT }
            }
            steps {
                sh '''set -eu
                chmod +x gradlew
                set +x
                . "$ENV_PATH"
                set -x

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

                docker run --rm \
                  -e PGPASSWORD="$MAIN_DB_PASSWORD" \
                  postgres:16-alpine \
                  sh -c "until psql -h $CI_DB_HOST -p $LOCAL_POSTGRES_PORT -U $MAIN_DB_USER -d main -c 'SELECT 1' >/dev/null 2>&1; do echo 'Waiting for PostgreSQL on mapped host port...'; sleep 2; done"

                ./gradlew --no-daemon clean :logic:flywayMigrate :logic:generateJooq build :app:shadowJar
                '''
            }
        }

        stage('Build Docker image') {
            when {
                expression { !params.DESTROY_DEPLOYMENT }
            }
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
            when {
                expression { !params.DESTROY_DEPLOYMENT }
            }
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

        stage('Destroy minikube deployment') {
            when {
                expression { params.DESTROY_DEPLOYMENT }
            }
            steps {
                sh '''set -eu
                if ! minikube -p "$MINIKUBE_PROFILE" status >/dev/null 2>&1; then
                  echo "Minikube profile '$MINIKUBE_PROFILE' not found. Nothing to delete."
                  exit 0
                fi

                kubectl config use-context "$MINIKUBE_PROFILE"
                kubectl delete namespace "$K8S_NAMESPACE" --ignore-not-found=true
                kubectl wait --for=delete namespace/"$K8S_NAMESPACE" --timeout=180s || true
                '''
            }
        }
    }

    post {
        failure {
            sh '''set +e
            if kubectl get namespace "$K8S_NAMESPACE" >/dev/null 2>&1; then
              kubectl -n "$K8S_NAMESPACE" get pods || true
              kubectl -n "$K8S_NAMESPACE" logs job/restobot-db-migrate || true
            fi

            docker logs "$LOCAL_POSTGRES_CONTAINER" || true
            exit 0
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
