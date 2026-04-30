pipeline {
    agent { label 'labs' }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    parameters {
        booleanParam(name: 'TRIGGER_BUILD_JOB', defaultValue: false, description: 'Trigger build job before downloading artifact')
        string(name: 'BUILD_JOB_NAME', defaultValue: 'Restobot', description: 'Name of the Jenkins build job from lab 2')
        string(name: 'BUILD_NUMBER', defaultValue: '', description: 'Specific build number to copy artifact from. Empty = last successful build')
        string(name: 'STACK_NAME', defaultValue: 'restobot-stack', description: 'Heat stack name used to resolve VM floating IP')
        string(name: 'TARGET_HOST', defaultValue: '', description: 'Optional explicit VM IP/hostname. Leave empty to resolve from Heat outputs')
        string(name: 'SERVICE_NAME', defaultValue: 'restobot', description: 'systemd service name on VM')
        string(name: 'APP_DIR', defaultValue: '/opt/restobot', description: 'Application directory on VM')
        string(name: 'APP_USER', defaultValue: 'restobot', description: 'Linux user that will own files and run the service')
        string(name: 'APP_PORT', defaultValue: '8089', description: 'Application port for healthcheck')
    }

    environment {
        OPENSTACK_RC_PATH = "${WORKSPACE}/openstack.rc"
        ENV_PATH = "${WORKSPACE}/deploy/.env.vm"
        ARTIFACT_DIR = "${WORKSPACE}/deploy/.tmp"
        ARTIFACT_PATH = "${WORKSPACE}/deploy/.tmp/app-fat.jar"
        TARGET_HOST_RESOLVED = ''
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Deploy Inputs') {
            steps {
                withCredentials([file(credentialsId: 'restobot_env', variable: 'ENV_FILE')]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    mkdir -p "$ARTIFACT_DIR"
                    perl -pe 's/\r$//' "$ENV_FILE" > "$ENV_PATH"
                    '''
                }
            }
        }

        stage('Trigger Build Job') {
            when {
                expression { params.TRIGGER_BUILD_JOB }
            }
            steps {
                build job: params.BUILD_JOB_NAME, wait: true, propagate: true
            }
        }

        stage('Copy Artifact From Build Job') {
            steps {
                script {
                    def selectorToUse = params.BUILD_NUMBER?.trim() ? specific(params.BUILD_NUMBER.trim()) : lastSuccessful()
                    copyArtifacts(
                        projectName: params.BUILD_JOB_NAME,
                        selector: selectorToUse,
                        filter: 'app/build/libs/app-fat.jar',
                        target: 'deploy/.tmp',
                        fingerprintArtifacts: true,
                        flatten: true
                    )
                }

                sh '''#!/usr/bin/env bash
                set -euo pipefail
                test -f "$ARTIFACT_PATH"
                ls -l "$ARTIFACT_PATH"
                '''
            }
        }

        stage('Resolve Target Host') {
            steps {
                script {
                    def targetHostParam = params.TARGET_HOST?.trim()
                    def targetHostEnv = env.TARGET_HOST?.trim()
                    def resolvedTargetHost = ''

                    echo "TARGET_HOST parameter: '${targetHostParam}'"

                    if (targetHostParam && targetHostParam != 'null') {
                        resolvedTargetHost = targetHostParam
                    } else if (targetHostEnv && targetHostEnv != 'null') {
                        resolvedTargetHost = targetHostEnv
                    } else {
                        withCredentials([
                            file(credentialsId: 'openstack_rc', variable: 'OPENSTACK_RC_FILE'),
                            string(credentialsId: 'openstack_password', variable: 'OPENSTACK_PASSWORD')
                        ]) {
                            sh '''#!/usr/bin/env bash
                            set -euo pipefail
                            perl -ne 's/\\r$//; next if /Please enter your OpenStack Password/; next if /read .*OS_PASSWORD/; next if /OS_PASSWORD_INPUT/; print' "$OPENSTACK_RC_FILE" > "$OPENSTACK_RC_PATH"
                            printf '\\nexport OS_PASSWORD=%q\\n' "$OPENSTACK_PASSWORD" >> "$OPENSTACK_RC_PATH"
                            . "$OPENSTACK_RC_PATH"
                            openstack stack output show "$STACK_NAME" floating_ip -f value -c output_value > "$ARTIFACT_DIR/target_host.txt"
                            '''
                        }
                        resolvedTargetHost = readFile('deploy/.tmp/target_host.txt').trim()
                    }

                    if (!resolvedTargetHost || resolvedTargetHost == 'null') {
                        error("Could not resolve target host. Set TARGET_HOST manually or check Heat output '${params.STACK_NAME}.floating_ip'.")
                    }

                    env.TARGET_HOST_RESOLVED = resolvedTargetHost
                }

                echo "Resolved target host: ${env.TARGET_HOST_RESOLVED}"
            }
        }

        stage('Deploy Artifact') {
            steps {
                withCredentials([
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')
                ]) {
                    withEnv([
                        "TARGET_HOST=${env.TARGET_HOST_RESOLVED}",
                        "SSH_USER=${env.SSH_USER}",
                        "SSH_KEY_PATH=${env.SSH_KEY}",
                        "ARTIFACT_PATH=${env.ARTIFACT_PATH}",
                        "ENV_FILE_PATH=${env.ENV_PATH}",
                        "APP_DIR=${params.APP_DIR}",
                        "APP_USER=${params.APP_USER}",
                        "SERVICE_NAME=${params.SERVICE_NAME}",
                        "APP_PORT=${params.APP_PORT}"
                    ]) {
                        sh '''#!/usr/bin/env bash
                        set -euo pipefail
                        chmod +x deploy/deploy.sh
                        deploy/deploy.sh
                        '''
                    }
                }
            }
        }

        stage('External Healthcheck') {
            steps {
                withEnv(["TARGET_HOST=${env.TARGET_HOST_RESOLVED}"]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    for _ in $(seq 1 20); do
                      if curl -fsS "http://${TARGET_HOST}:${APP_PORT}/healthcheck"; then
                        exit 0
                      fi
                      sleep 3
                    done

                    echo "External healthcheck failed for ${TARGET_HOST}:${APP_PORT}" >&2
                    exit 1
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'deploy/.tmp/app-fat.jar', allowEmptyArchive: true, fingerprint: true
            deleteDir()
        }
    }
}
