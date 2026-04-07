pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    parameters {
        choice(name: 'TF_ACTION', choices: ['apply', 'destroy'], description: 'Create/update or destroy infrastructure')
        booleanParam(name: 'RUN_BUILD', defaultValue: false, description: 'Run Gradle build before provisioning')
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
        TF_IN_AUTOMATION = 'true'
        TF_CLI_CONFIG_FILE = "${WORKSPACE}/terraform.tfrc"
        ANSIBLE_CONFIG = "${WORKSPACE}/ansible/ansible.cfg"
        ENV_PATH = "${WORKSPACE}/.env"
        TFVARS_PATH = "${WORKSPACE}/terraform/jenkins.auto.tfvars"
        GENERATED_SSH_DIR = "${WORKSPACE}/.keys"
        GENERATED_SSH_PUBLIC_KEY = "${WORKSPACE}/.keys/restobot.pub"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Credentials') {
            steps {
                withCredentials([
                    file(credentialsId: 'restobot_env', variable: 'ENV_FILE'),
                    file(credentialsId: 'restobot_tfvars', variable: 'TFVARS_FILE'),
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')
                ]) {
                    sh '''#!/bin/bash
                    set -euo pipefail
                    mkdir -p "$GENERATED_SSH_DIR"
                    perl -pe 's/\r$//' "$ENV_FILE" > "$ENV_PATH"
                    perl -pe 's/\r$//' "$TFVARS_FILE" > "$TFVARS_PATH"
                    ssh-keygen -y -f "$SSH_KEY_FILE" > "$GENERATED_SSH_PUBLIC_KEY"
                    chmod 600 "$SSH_KEY_FILE" "$GENERATED_SSH_PUBLIC_KEY" "$ENV_PATH" "$TFVARS_PATH"
                    '''
                }
            }
        }

        stage('Validate Tooling') {
            steps {
                sh '''#!/bin/bash
                set -euo pipefail
                terraform version
                ansible-playbook --version
                ssh -V || true
                '''
            }
        }

        stage('Gradle Prep') {
            when {
                expression { params.RUN_BUILD }
            }
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Prepare Local Database') {
            when {
                expression { params.RUN_BUILD }
            }
            steps {
                sh '''#!/bin/bash
                set -euo pipefail
                docker compose up -d postgres

                POSTGRES_CONTAINER_ID="$(docker compose ps -q postgres)"
                if [ -z "$POSTGRES_CONTAINER_ID" ]; then
                  echo "Failed to start local PostgreSQL container."
                  exit 1
                fi

                for attempt in $(seq 1 24); do
                  STATUS="$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$POSTGRES_CONTAINER_ID")"
                  if [ "$STATUS" = "healthy" ]; then
                    ./gradlew :logic:flywayMigrate
                    exit 0
                  fi
                  sleep 5
                done

                echo "Local PostgreSQL is still unavailable after waiting."
                exit 1
                '''
            }
        }

        stage('Build') {
            when {
                expression { params.RUN_BUILD }
            }
            steps {
                sh './gradlew build'
            }
        }

        stage('Terraform Init') {
            steps {
                dir('terraform') {
                    sh 'terraform init'
                }
            }
        }

        stage('Terraform Validate') {
            steps {
                dir('terraform') {
                    sh 'terraform fmt -check'
                    sh 'terraform validate'
                }
            }
        }

        stage('Terraform Plan') {
            when {
                expression { params.TF_ACTION == 'apply' }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'yc_iam_token', variable: 'YC_IAM_TOKEN'),
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')
                ]) {
                    withEnv([
                        "YC_TOKEN=${YC_IAM_TOKEN}",
                        "TF_VAR_ssh_public_key_path=${env.GENERATED_SSH_PUBLIC_KEY}",
                        "TF_VAR_ssh_user=${SSH_USER}"
                    ]) {
                        dir('terraform') {
                            sh 'terraform plan -var-file=jenkins.auto.tfvars -out=tfplan'
                        }
                    }
                }
            }
        }

        stage('Terraform Apply') {
            when {
                expression { params.TF_ACTION == 'apply' }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'yc_iam_token', variable: 'YC_IAM_TOKEN'),
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')
                ]) {
                    withEnv([
                        "YC_TOKEN=${YC_IAM_TOKEN}",
                        "TF_VAR_ssh_public_key_path=${env.GENERATED_SSH_PUBLIC_KEY}",
                        "TF_VAR_ssh_user=${SSH_USER}"
                    ]) {
                        dir('terraform') {
                            sh 'terraform apply -auto-approve tfplan'
                        }
                    }
                }
            }
        }

        stage('Wait For SSH') {
            when {
                expression { params.TF_ACTION == 'apply' }
            }
            steps {
                withCredentials([
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')
                ]) {
                    sh '''#!/bin/bash
                    set -euo pipefail
                    for attempt in $(seq 1 20); do
                      ansible -i ansible/inventory/hosts.ini restobot -m ping --private-key "$SSH_KEY_FILE" -u "$SSH_USER" && exit 0
                      sleep 15
                    done
                    echo "SSH is still unavailable after waiting."
                    exit 1
                    '''
                }
            }
        }

        stage('Configure Hosts With Ansible') {
            when {
                expression { params.TF_ACTION == 'apply' }
            }
            steps {
                withCredentials([
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')
                ]) {
                    sh '''#!/bin/bash
                    set -euo pipefail
                    ansible-playbook -i ansible/inventory/hosts.ini ansible/playbook.yml --private-key "$SSH_KEY_FILE" -u "$SSH_USER"
                    '''
                }
            }
        }

        stage('Terraform Output') {
            when {
                expression { params.TF_ACTION == 'apply' }
            }
            steps {
                dir('terraform') {
                    sh 'terraform output'
                }
            }
        }

        stage('Terraform Destroy') {
            when {
                expression { params.TF_ACTION == 'destroy' }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'yc_iam_token', variable: 'YC_IAM_TOKEN'),
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY_FILE', usernameVariable: 'SSH_USER')
                ]) {
                    withEnv([
                        "YC_TOKEN=${YC_IAM_TOKEN}",
                        "TF_VAR_ssh_public_key_path=${env.GENERATED_SSH_PUBLIC_KEY}",
                        "TF_VAR_ssh_user=${SSH_USER}"
                    ]) {
                        dir('terraform') {
                            sh 'terraform destroy -auto-approve -var-file=jenkins.auto.tfvars'
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            sh '''#!/bin/bash
            set -euo pipefail
            rm -f "$ENV_PATH" "$TFVARS_PATH"
            rm -rf "$GENERATED_SSH_DIR"
            '''
        }
    }
}
