pipeline {
    agent { label 'gaar-agent' }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    parameters {
        choice(name: 'TF_ACTION', choices: ['apply', 'destroy'], description: 'Terraform action to execute')
        booleanParam(name: 'RUN_BUILD', defaultValue: true, description: 'Run Gradle build before infra/deploy when TF_ACTION=apply')
        booleanParam(name: 'RUN_ANSIBLE', defaultValue: true, description: 'Run ansible deploy when TF_ACTION=apply')
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        JAVA_HOME = "/usr/lib/jvm/temurin-23-jdk-amd64"
        PATH = "${JAVA_HOME}/bin:${PATH}"
        TF_IN_AUTOMATION = "true"
        ENV_PATH = "${WORKSPACE}/.env"
        TF_DIR = "${WORKSPACE}/terraform"
        TFVARS_PATH = "${WORKSPACE}/terraform/terraform.tfvars"
        TFPLAN_PATH = "${WORKSPACE}/terraform/tfplan"
        ANSIBLE_DIR = "${WORKSPACE}/ansible"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Secrets') {
            steps {
                withCredentials([
                    file(credentialsId: 'restobot_env', variable: 'ENV_FILE'),
                    file(credentialsId: 'restobot_tfvars', variable: 'TFVARS_FILE')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    perl -pe 's/\r$//' "$ENV_FILE" > "$ENV_PATH"
                    perl -pe 's/\r$//' "$TFVARS_FILE" > "$TFVARS_PATH"
                    ls -l "$ENV_PATH" "$TFVARS_PATH"
                    '''
                }
            }
        }

        stage('Build') {
            when {
                expression { params.TF_ACTION == 'apply' && params.RUN_BUILD }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                chmod +x gradlew

                # Build needs a local Postgres for flyway + jOOQ generation.
                docker-compose down -v || true
                docker-compose up -d postgres

                for i in $(seq 1 30); do
                  if docker-compose exec -T postgres sh -lc 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"' >/dev/null 2>&1; then
                    break
                  fi
                  sleep 2
                done

                # Force local build DB URL to Docker-mapped port.
                if grep -q '^MAIN_DB_URL=' "$ENV_PATH"; then
                  sed -i 's#^MAIN_DB_URL=.*#MAIN_DB_URL=jdbc:postgresql://localhost:5435/main#' "$ENV_PATH"
                else
                  echo 'MAIN_DB_URL=jdbc:postgresql://localhost:5435/main' >> "$ENV_PATH"
                fi

                ./gradlew :logic:flywayMigrate :logic:generateJooq build
                '''
            }
        }

        stage('Terraform Init') {
            steps {
                withCredentials([
                    string(credentialsId: 'yc_iam_token', variable: 'YC_TOKEN'),
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    chmod 600 "$SSH_KEY"
                    ssh-keygen -y -f "$SSH_KEY" > "$TF_DIR/ci_id_ed25519.pub"

                    if grep -q '^ssh_public_key_path' "$TFVARS_PATH"; then
                      sed -i "s#^ssh_public_key_path.*#ssh_public_key_path = \\"$TF_DIR/ci_id_ed25519.pub\\"#" "$TFVARS_PATH"
                    else
                      echo "ssh_public_key_path = \\"$TF_DIR/ci_id_ed25519.pub\\"" >> "$TFVARS_PATH"
                    fi

                    export YC_TOKEN
                    terraform -chdir="$TF_DIR" init
                    terraform -chdir="$TF_DIR" validate
                    '''
                }
            }
        }

        stage('Terraform Plan') {
            when {
                expression { params.TF_ACTION == 'apply' }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'yc_iam_token', variable: 'YC_TOKEN')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    export YC_TOKEN
                    terraform -chdir="$TF_DIR" plan -out="$TFPLAN_PATH"
                    '''
                }
            }
        }

        stage('Terraform Apply') {
            when {
                expression { params.TF_ACTION == 'apply' }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'yc_iam_token', variable: 'YC_TOKEN')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    export YC_TOKEN
                    terraform -chdir="$TF_DIR" apply -auto-approve "$TFPLAN_PATH"
                    '''
                }
            }
        }

        stage('Ansible Deploy') {
            when {
                expression { params.TF_ACTION == 'apply' && params.RUN_ANSIBLE }
            }
            steps {
                withCredentials([
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    test -f "$ANSIBLE_DIR/inventory/hosts.ini"
                    ANSIBLE_CONFIG="$ANSIBLE_DIR/ansible.cfg" ansible-playbook "$ANSIBLE_DIR/playbook.yml" \
                      --private-key "$SSH_KEY" \
                      -u "$SSH_USER"
                    '''
                }
            }
        }

        stage('Smoke Test') {
            when {
                expression { params.TF_ACTION == 'apply' && params.RUN_ANSIBLE }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail

                HOST=$(awk '/ansible_host=/{for(i=1;i<=NF;i++) if($i ~ /^ansible_host=/){split($i,a,"="); print a[2]; exit}}' "$ANSIBLE_DIR/inventory/hosts.ini")
                if [ -z "${HOST:-}" ]; then
                  echo "Failed to extract ansible_host from inventory."
                  exit 1
                fi

                echo "Checking http://$HOST:8089/healthcheck"
                for i in $(seq 1 20); do
                  if curl -fsS "http://$HOST:8089/healthcheck"; then
                    echo
                    exit 0
                  fi
                  sleep 5
                done

                echo "Smoke test failed."
                exit 1
                '''
            }
        }

        stage('Terraform Destroy') {
            when {
                expression { params.TF_ACTION == 'destroy' }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'yc_iam_token', variable: 'YC_TOKEN')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    export YC_TOKEN
                    terraform -chdir="$TF_DIR" destroy -auto-approve
                    '''
                }
            }
        }
    }

    post {
        always {
            sh '''#!/usr/bin/env bash
            set +e
            docker-compose down -v
            '''
            archiveArtifacts artifacts: 'app/build/libs/*.jar, terraform/tfplan, ansible/inventory/hosts.ini', allowEmptyArchive: true, fingerprint: true
            deleteDir()
        }
    }
}
