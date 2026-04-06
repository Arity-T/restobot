pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    parameters {
        choice(name: 'K8S_ACTION', choices: ['deploy', 'delete'], description: 'Deploy app to Kubernetes or delete deployed resources')
        booleanParam(name: 'RUN_BUILD', defaultValue: true, description: 'Run Gradle build before image build')
        booleanParam(name: 'PUSH_IMAGE', defaultValue: false, description: 'Push image to registry (requires docker_registry credentials)')
        string(name: 'IMAGE_REPOSITORY', defaultValue: 'restobot-app', description: 'Docker image repository, e.g. restobot-app or dockerhub_user/restobot-app')
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'Image tag. Leave empty to use build-<BUILD_NUMBER>')
        string(name: 'K8S_NAMESPACE', defaultValue: 'restobot', description: 'Kubernetes namespace for deployment')
        booleanParam(name: 'DELETE_NAMESPACE', defaultValue: false, description: 'Delete namespace during K8S_ACTION=delete')
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        ENV_PATH = "${WORKSPACE}/.env"
        KUBECONFIG_PATH = "${WORKSPACE}/.kubeconfig"
        K8S_DIR = "${WORKSPACE}/k8s"
        DOCKER_IMAGE = ''
        IMAGE_PULL_POLICY = 'IfNotPresent'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Kubeconfig') {
            steps {
                withCredentials([
                    file(credentialsId: 'restobot_kubeconfig', variable: 'KUBECONFIG_FILE')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    perl -pe 's/\r$//' "$KUBECONFIG_FILE" > "$KUBECONFIG_PATH"
                    chmod 600 "$KUBECONFIG_PATH"
                    '''
                }
            }
        }

        stage('Prepare App Env') {
            when {
                expression { params.K8S_ACTION == 'deploy' }
            }
            steps {
                withCredentials([
                    file(credentialsId: 'restobot_env', variable: 'ENV_FILE')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    perl -pe 's/\r$//' "$ENV_FILE" > "$ENV_PATH"

                    if grep -q '^API_SERVER_HOST=' "$ENV_PATH"; then
                      sed -i 's#^API_SERVER_HOST=.*#API_SERVER_HOST=0.0.0.0#' "$ENV_PATH"
                    else
                      echo 'API_SERVER_HOST=0.0.0.0' >> "$ENV_PATH"
                    fi

                    if grep -q '^API_SERVER_PORT=' "$ENV_PATH"; then
                      sed -i 's#^API_SERVER_PORT=.*#API_SERVER_PORT=8089#' "$ENV_PATH"
                    else
                      echo 'API_SERVER_PORT=8089' >> "$ENV_PATH"
                    fi
                    '''
                }
            }
        }

        stage('Build') {
            when {
                expression { params.K8S_ACTION == 'deploy' && params.RUN_BUILD }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
                export PATH="$JAVA_HOME/bin:$PATH"
                java -version
                chmod +x gradlew

                docker-compose down -v || true
                docker-compose up -d postgres

                READY=0
                for i in $(seq 1 30); do
                  if docker-compose exec -T postgres sh -lc 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"' >/dev/null 2>&1; then
                    READY=1
                    break
                  fi
                  sleep 2
                done

                if [ "$READY" -ne 1 ]; then
                  echo "Postgres did not become ready in time"
                  docker-compose logs --tail=100 postgres || true
                  exit 1
                fi

                DB_PORT="$(docker-compose port postgres 5432 | tail -n1 | sed -E 's/.*:([0-9]+)$/\\1/')"
                if [ -z "${DB_PORT:-}" ]; then
                  echo "Failed to determine mapped Postgres port"
                  docker-compose ps
                  exit 1
                fi

                DB_HOST="localhost"
                if [ -f "/.dockerenv" ] && getent hosts host.docker.internal >/dev/null 2>&1; then
                  DB_HOST="host.docker.internal"
                fi

                DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/main"
                echo "Using build DB URL: ${DB_URL}"

                if grep -q '^MAIN_DB_URL=' "$ENV_PATH"; then
                  sed -i "s#^MAIN_DB_URL=.*#MAIN_DB_URL=${DB_URL}#" "$ENV_PATH"
                else
                  echo "MAIN_DB_URL=${DB_URL}" >> "$ENV_PATH"
                fi

                ./gradlew --no-daemon -PjavaToolchainVersion=21 :logic:flywayMigrate :logic:generateJooq build
                '''
            }
        }

        stage('Build Docker Image') {
            when {
                expression { params.K8S_ACTION == 'deploy' }
            }
            steps {
                script {
                    def resolvedTag = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : "build-${env.BUILD_NUMBER}"
                    env.DOCKER_IMAGE = "${params.IMAGE_REPOSITORY}:${resolvedTag}"
                    env.IMAGE_PULL_POLICY = params.PUSH_IMAGE ? 'Always' : 'IfNotPresent'
                }

                sh '''#!/usr/bin/env bash
                set -euo pipefail

                if [ ! -f "app/build/libs/app-fat.jar" ]; then
                  echo "Missing app/build/libs/app-fat.jar. Enable RUN_BUILD or build artifact beforehand."
                  exit 1
                fi

                echo "Building image: $DOCKER_IMAGE"
                docker build -t "$DOCKER_IMAGE" .
                '''
            }
        }

        stage('Push Docker Image') {
            when {
                expression { params.K8S_ACTION == 'deploy' && params.PUSH_IMAGE }
            }
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'docker_registry', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail

                    REGISTRY="$(echo "$DOCKER_IMAGE" | awk -F/ '{if (NF>1 && $1 ~ /[.:]/) print $1; else print "docker.io"}')"
                    echo "$DOCKER_PASS" | docker login "$REGISTRY" -u "$DOCKER_USER" --password-stdin
                    docker push "$DOCKER_IMAGE"
                    docker logout "$REGISTRY" || true
                    '''
                }
            }
        }

        stage('Deploy To Kubernetes') {
            when {
                expression { params.K8S_ACTION == 'deploy' }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                chmod +x scripts/kctl.sh
                export KUBECONFIG_PATH

                scripts/kctl.sh version --client

                if ! scripts/kctl.sh get namespace "$K8S_NAMESPACE" >/dev/null 2>&1; then
                  scripts/kctl.sh create namespace "$K8S_NAMESPACE"
                fi

                scripts/kctl.sh -n "$K8S_NAMESPACE" create secret generic restobot-env \
                  --from-env-file="$ENV_PATH" \
                  --dry-run=client -o yaml | scripts/kctl.sh apply -f -

                scripts/kctl.sh -n "$K8S_NAMESPACE" create configmap restobot-db-migrations \
                  --from-file=logic/src/main/resources/db/migration/main/V1__init_main.sql \
                  --from-file=logic/src/main/resources/db/migration/main/V2__add_data.sql \
                  --dry-run=client -o yaml | scripts/kctl.sh apply -f -

                scripts/kctl.sh -n "$K8S_NAMESPACE" apply -f "$K8S_DIR/postgres.yaml"
                scripts/kctl.sh -n "$K8S_NAMESPACE" rollout status deployment/restobot-postgres --timeout=240s

                scripts/kctl.sh -n "$K8S_NAMESPACE" delete job restobot-db-init --ignore-not-found=true
                scripts/kctl.sh -n "$K8S_NAMESPACE" apply -f "$K8S_DIR/db-init-job.yaml"

                if ! scripts/kctl.sh -n "$K8S_NAMESPACE" wait --for=condition=complete job/restobot-db-init --timeout=300s; then
                  scripts/kctl.sh -n "$K8S_NAMESPACE" logs job/restobot-db-init || true
                  exit 1
                fi

                mkdir -p "$K8S_DIR/.rendered"
                sed -e "s|__APP_IMAGE__|$DOCKER_IMAGE|g" \
                    -e "s|__IMAGE_PULL_POLICY__|$IMAGE_PULL_POLICY|g" \
                    "$K8S_DIR/app-deployment.yaml" > "$K8S_DIR/.rendered/app-deployment.yaml"

                scripts/kctl.sh -n "$K8S_NAMESPACE" apply -f "$K8S_DIR/.rendered/app-deployment.yaml"
                scripts/kctl.sh -n "$K8S_NAMESPACE" apply -f "$K8S_DIR/app-service.yaml"
                scripts/kctl.sh -n "$K8S_NAMESPACE" rollout status deployment/restobot-app --timeout=300s
                scripts/kctl.sh -n "$K8S_NAMESPACE" get svc restobot-app -o wide
                '''
            }
        }

        stage('Smoke Test') {
            when {
                expression { params.K8S_ACTION == 'deploy' }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                chmod +x scripts/kctl.sh
                export KUBECONFIG_PATH

                scripts/kctl.sh -n "$K8S_NAMESPACE" delete pod restobot-smoke --ignore-not-found=true

                scripts/kctl.sh -n "$K8S_NAMESPACE" run restobot-smoke \
                  --image=curlimages/curl:8.8.0 \
                  --restart=Never \
                  --command -- sh -c 'for i in $(seq 1 24); do curl -fsS http://restobot-app:8089/healthcheck && exit 0; sleep 5; done; exit 1'

                if ! scripts/kctl.sh -n "$K8S_NAMESPACE" wait --for=jsonpath='{.status.phase}'=Succeeded pod/restobot-smoke --timeout=180s; then
                  scripts/kctl.sh -n "$K8S_NAMESPACE" logs restobot-smoke || true
                  scripts/kctl.sh -n "$K8S_NAMESPACE" delete pod restobot-smoke --ignore-not-found=true || true
                  exit 1
                fi

                scripts/kctl.sh -n "$K8S_NAMESPACE" logs restobot-smoke
                scripts/kctl.sh -n "$K8S_NAMESPACE" delete pod restobot-smoke --ignore-not-found=true
                '''
            }
        }

        stage('Delete Kubernetes Resources') {
            when {
                expression { params.K8S_ACTION == 'delete' }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                chmod +x scripts/kctl.sh
                export KUBECONFIG_PATH

                scripts/kctl.sh -n "$K8S_NAMESPACE" delete deployment restobot-app --ignore-not-found=true
                scripts/kctl.sh -n "$K8S_NAMESPACE" delete service restobot-app --ignore-not-found=true
                scripts/kctl.sh -n "$K8S_NAMESPACE" delete job restobot-db-init --ignore-not-found=true
                scripts/kctl.sh -n "$K8S_NAMESPACE" delete deployment restobot-postgres --ignore-not-found=true
                scripts/kctl.sh -n "$K8S_NAMESPACE" delete service restobot-postgres --ignore-not-found=true
                scripts/kctl.sh -n "$K8S_NAMESPACE" delete configmap restobot-db-migrations --ignore-not-found=true
                scripts/kctl.sh -n "$K8S_NAMESPACE" delete secret restobot-env --ignore-not-found=true

                if [ "$DELETE_NAMESPACE" = "true" ]; then
                  scripts/kctl.sh delete namespace "$K8S_NAMESPACE" --ignore-not-found=true
                fi
                '''
            }
        }
    }

    post {
        always {
            sh '''#!/usr/bin/env bash
            set +e
            docker-compose down -v
            '''
            archiveArtifacts artifacts: 'app/build/libs/*.jar, k8s/.rendered/*.yaml', allowEmptyArchive: true, fingerprint: true
        }
    }
}
