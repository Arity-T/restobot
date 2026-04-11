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
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        RAW_ENV_PATH = "${WORKSPACE}/.env.k8s"
        ENV_PATH = "${WORKSPACE}/.env"
        BUILD_DB_HOST = 'localhost'
        K8S_NAMESPACE = 'restobot'
        LOCAL_POSTGRES_CONTAINER = 'restobot-ci-postgres'
        LOCAL_BUILD_NETWORK = 'restobot-ci-net'
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
                    grep -Ev '^(export[[:space:]]+)?MAIN_DB_URL[[:space:]]*=' "$RAW_ENV_PATH" > "$ENV_PATH" || true
                    printf 'MAIN_DB_URL=jdbc:postgresql://%s:%s/main\n' "$BUILD_DB_HOST" "$LOCAL_POSTGRES_PORT" >> "$ENV_PATH"
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
                . "$ENV_PATH"
                export MAIN_DB_URL="jdbc:postgresql://$BUILD_DB_HOST:$LOCAL_POSTGRES_PORT/main"
                export MAIN_DB_USER
                export MAIN_DB_PASSWORD

                echo "Using CI MAIN_DB_URL=$MAIN_DB_URL"
                echo "Using Docker network '$LOCAL_BUILD_NETWORK' for Gradle database tasks"

                docker rm -f "$LOCAL_POSTGRES_CONTAINER" >/dev/null 2>&1 || true
                docker network create "$LOCAL_BUILD_NETWORK" >/dev/null 2>&1 || true
                docker run -d \
                  --name "$LOCAL_POSTGRES_CONTAINER" \
                  --network "$LOCAL_BUILD_NETWORK" \
                  --network-alias "$LOCAL_POSTGRES_CONTAINER" \
                  -e POSTGRES_USER="$MAIN_DB_USER" \
                  -e POSTGRES_PASSWORD="$MAIN_DB_PASSWORD" \
                  -e POSTGRES_DB=postgres \
                  -p "$LOCAL_POSTGRES_PORT":5432 \
                  postgres:16-alpine >/dev/null

                until docker exec "$LOCAL_POSTGRES_CONTAINER" pg_isready -U "$MAIN_DB_USER" -d postgres >/dev/null 2>&1; do
                  echo "Waiting for local PostgreSQL..."
                  sleep 3
                done

                if ! docker exec -e PGPASSWORD="$MAIN_DB_PASSWORD" "$LOCAL_POSTGRES_CONTAINER" \
                  psql -U "$MAIN_DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'main'" | grep -q 1; then
                  docker exec -e PGPASSWORD="$MAIN_DB_PASSWORD" "$LOCAL_POSTGRES_CONTAINER" \
                    psql -U "$MAIN_DB_USER" -d postgres -c "CREATE DATABASE main"
                fi

                attempts=0
                until nc -z "$BUILD_DB_HOST" "$LOCAL_POSTGRES_PORT"; do
                  attempts=$((attempts + 1))
                  if [ "$attempts" -ge 60 ]; then
                    echo "PostgreSQL is not reachable at $BUILD_DB_HOST:$LOCAL_POSTGRES_PORT"
                    docker logs "$LOCAL_POSTGRES_CONTAINER" || true
                    exit 1
                  fi
                  echo "Waiting for PostgreSQL on $BUILD_DB_HOST:$LOCAL_POSTGRES_PORT..."
                  sleep 2
                done

                PGPASSWORD="$MAIN_DB_PASSWORD" psql \
                  -h "$BUILD_DB_HOST" \
                  -p "$LOCAL_POSTGRES_PORT" \
                  -U "$MAIN_DB_USER" \
                  -d main \
                  -c 'SELECT 1'

                echo "Running Gradle build in a temporary JDK container..."
                docker run --rm \
                  --user "$(id -u):$(id -g)" \
                  --network "$LOCAL_BUILD_NETWORK" \
                  -e GRADLE_USER_HOME=/workspace/.gradle \
                  -e MAIN_DB_URL="jdbc:postgresql://$LOCAL_POSTGRES_CONTAINER:5432/main" \
                  -e MAIN_DB_USER="$MAIN_DB_USER" \
                  -e MAIN_DB_PASSWORD="$MAIN_DB_PASSWORD" \
                  -v "$WORKSPACE":/workspace \
                  -w /workspace \
                  eclipse-temurin:23-jdk \
                  sh -lc './gradlew --no-daemon clean :logic:flywayMigrate :logic:generateJooq build :app:shadowJar'
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

        cleanup {
            sh '''set +e
            docker rm -f "$LOCAL_POSTGRES_CONTAINER" >/dev/null 2>&1 || true
            docker network rm "$LOCAL_BUILD_NETWORK" >/dev/null 2>&1 || true
            '''
            deleteDir()
        }
    }
}
