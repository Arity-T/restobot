pipeline {
    agent { label 'tishenko' }

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
        TF_IN_AUTOMATION = "true"
        ENV_PATH = "${WORKSPACE}/.env"
        TF_DIR = "${WORKSPACE}/terraform"
        TFVARS_PATH = "${WORKSPACE}/terraform/terraform.tfvars"
        TFPLAN_PATH = "${WORKSPACE}/terraform/tfplan"
        ANSIBLE_DIR = "${WORKSPACE}/ansible"
        TF_CLI_CONFIG_FILE = "${WORKSPACE}/.terraformrc"
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
                    file(credentialsId: 'restobot_tfvars', variable: 'TFVARS_FILE'),
                    file(credentialsId: 'restobot_yc_sa_key', variable: 'SA_KEY_FILE')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    perl -pe 's/\r$//' "$ENV_FILE" > "$ENV_PATH"
                    perl -pe 's/\r$//' "$TFVARS_FILE" > "$TFVARS_PATH"
                    cp "$SA_KEY_FILE" "$TF_DIR/authorized_key.json"
                    chmod 600 "$TF_DIR/authorized_key.json"
                    ls -l "$ENV_PATH" "$TFVARS_PATH" "$TF_DIR/authorized_key.json"
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
                export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")"
                export PATH="$JAVA_HOME/bin:$PATH"
                java -version
                chmod +x gradlew

                # Build needs a local Postgres for flyway + jOOQ generation.
                docker compose down -v || true
                docker compose up -d postgres

                READY=0
                for i in $(seq 1 30); do
                  if docker compose exec -T postgres sh -lc 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"' >/dev/null 2>&1; then
                    READY=1
                    break
                  fi
                  sleep 2
                done

                if [ "$READY" -ne 1 ]; then
                  echo "Postgres did not become ready in time"
                  docker compose logs --tail=100 postgres || true
                  exit 1
                fi

                DB_PORT="$(docker compose port postgres 5432 | tail -n1 | sed -E 's/.*:([0-9]+)$/\\1/')"
                if [ -z "${DB_PORT:-}" ]; then
                  echo "Failed to determine mapped Postgres port"
                  docker compose ps
                  exit 1
                fi

                DB_HOST="localhost"
                if [ -f "/.dockerenv" ] && getent hosts host.docker.internal >/dev/null 2>&1; then
                  DB_HOST="host.docker.internal"
                fi

                DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/main"
                echo "Using build DB URL: ${DB_URL}"

                # Force local build DB URL.
                if grep -q '^MAIN_DB_URL=' "$ENV_PATH"; then
                  sed -i "s#^MAIN_DB_URL=.*#MAIN_DB_URL=${DB_URL}#" "$ENV_PATH"
                else
                  echo "MAIN_DB_URL=${DB_URL}" >> "$ENV_PATH"
                fi

                ./gradlew --no-daemon -PjavaToolchainVersion=21 :logic:flywayMigrate :logic:generateJooq build
                '''
            }
        }

        stage('Terraform Init') {
            steps {
                withCredentials([
                    file(credentialsId: 'restobot_vm_ssh_pub', variable: 'SSH_PUB_FILE')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    perl -pe 's/\r$//' "$SSH_PUB_FILE" > "$TF_DIR/ci_id_ed25519.pub"
                    chmod 644 "$TF_DIR/ci_id_ed25519.pub"

                    if grep -q '^ssh_public_key_path' "$TFVARS_PATH"; then
                      sed -i "s#^ssh_public_key_path.*#ssh_public_key_path = \\"$TF_DIR/ci_id_ed25519.pub\\"#" "$TFVARS_PATH"
                    else
                      echo "ssh_public_key_path = \\"$TF_DIR/ci_id_ed25519.pub\\"" >> "$TFVARS_PATH"
                    fi

                    cat > "$TF_CLI_CONFIG_FILE" <<'EOF'
provider_installation {
  network_mirror {
    url     = "https://terraform-mirror.yandexcloud.net/"
    include = ["registry.terraform.io/*/*"]
  }
  direct {
    exclude = ["registry.terraform.io/*/*"]
  }
}
EOF

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
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                terraform -chdir="$TF_DIR" plan -out="$TFPLAN_PATH"
                '''
            }
        }

        stage('Terraform Apply') {
            when {
                expression { params.TF_ACTION == 'apply' }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                terraform -chdir="$TF_DIR" apply -auto-approve "$TFPLAN_PATH"
                '''
            }
        }

        stage('Wait For SSH') {
            when {
                expression { params.TF_ACTION == 'apply' && params.RUN_ANSIBLE }
            }
            steps {
                withCredentials([
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_CRED_USERNAME')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    chmod 600 "$SSH_KEY"
                    echo "Ansible inventory (ansible_user comes from Terraform / hosts.ini, not Jenkins credential username):"
                    cat "$ANSIBLE_DIR/inventory/hosts.ini"

                    for i in $(seq 1 20); do
                      if ANSIBLE_CONFIG="$ANSIBLE_DIR/ansible.cfg" ansible -i "$ANSIBLE_DIR/inventory/hosts.ini" restobot -m ping --private-key "$SSH_KEY" >/dev/null 2>&1; then
                        echo "SSH is available."
                        exit 0
                      fi
                      echo "Waiting for SSH... attempt $i/20"
                      sleep 15
                    done

                    echo "SSH is still unavailable after waiting. Last ansible ping (verbose):"
                    ANSIBLE_CONFIG="$ANSIBLE_DIR/ansible.cfg" ansible -i "$ANSIBLE_DIR/inventory/hosts.ini" restobot -m ping --private-key "$SSH_KEY" -vvv || true
                    exit 1
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
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_CRED_USERNAME')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    chmod 600 "$SSH_KEY"
                    test -f "$ANSIBLE_DIR/inventory/hosts.ini"
                    ANSIBLE_CONFIG="$ANSIBLE_DIR/ansible.cfg" ansible-playbook "$ANSIBLE_DIR/playbook.yml" \
                      --private-key "$SSH_KEY"
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
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                terraform -chdir="$TF_DIR" destroy -auto-approve
                '''
            }
        }
    }

    post {
        always {
            sh '''#!/usr/bin/env bash
            set +e
            docker compose down -v
            '''
            archiveArtifacts artifacts: 'app/build/libs/*.jar, terraform/tfplan, ansible/inventory/hosts.ini', allowEmptyArchive: true, fingerprint: true
        }
    }
}
