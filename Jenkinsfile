@NonCPS
String ensureLabsJenkinsNode(String nodeName, String nodeHost, String sshUser, String credentialsId, String privateKeyPath, String remoteRoot) {
    def nodeLabel = nodeName

    def credentialsProvider = com.cloudbees.plugins.credentials.SystemCredentialsProvider.getInstance()
    if (!credentialsProvider.getCredentials().any { credential -> credential.id == credentialsId }) {
        credentialsProvider.getCredentials().add(
            new com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey(
                com.cloudbees.plugins.credentials.CredentialsScope.GLOBAL,
                credentialsId,
                sshUser,
                new com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(privateKeyPath),
                '',
                'SSH key for Jenkins node labs'
            )
        )
        credentialsProvider.save()
    }

    def jenkinsInstance = jenkins.model.Jenkins.get()
    def existingNode = jenkinsInstance.getNode(nodeName)
    if (existingNode != null) {
        jenkinsInstance.removeNode(existingNode)
    }

    def launcher = new hudson.plugins.sshslaves.SSHLauncher(
        nodeHost,
        22,
        credentialsId,
        '',
        '',
        '',
        '',
        60,
        3,
        15
    )

    try {
        launcher.setSshHostKeyVerificationStrategy(
            new hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy()
        )
    } catch (ignored) {
        // Older SSH Build Agents plugin versions do not expose this setter.
    }

    def labsNode = new hudson.slaves.DumbSlave(
        nodeName,
        'Jenkins SSH node for labs server',
        remoteRoot,
        '1',
        hudson.model.Node.Mode.NORMAL,
        nodeLabel,
        launcher,
        new hudson.slaves.RetentionStrategy.Always()
    )

    jenkinsInstance.addNode(labsNode)
    return "${existingNode == null ? 'Created' : 'Updated'} Jenkins node '${nodeName}' for ${sshUser}@${nodeHost}"
}

pipeline {
    agent none

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    parameters {
        string(name: 'LABS_NODE_NAME', defaultValue: 'labs', description: 'Jenkins SSH node name and label')
        string(name: 'LABS_NODE_HOST', defaultValue: '192.168.24.19', description: 'OpenStack labs server IP used as Jenkins node')
        string(name: 'LABS_NODE_SSH_USER', defaultValue: 'ubuntu', description: 'SSH user for Jenkins node')
        string(name: 'LABS_NODE_CREDENTIALS_ID', defaultValue: 'labs-ssh-key', description: 'Credential ID for Jenkins node SSH key')
        string(name: 'LABS_NODE_PRIVATE_KEY_PATH', defaultValue: '/var/jenkins_home/.ssh/id_ed25519_labs', description: 'Private key path on Jenkins controller/container')
        string(name: 'LABS_NODE_REMOTE_ROOT', defaultValue: '/home/ubuntu/jenkins-agent', description: 'Remote root directory on Jenkins node')
    }

    stages {
        stage('Create Jenkins node') {
            agent any
            steps {
                script {
                    echo ensureLabsJenkinsNode(
                        params.LABS_NODE_NAME,
                        params.LABS_NODE_HOST,
                        params.LABS_NODE_SSH_USER,
                        params.LABS_NODE_CREDENTIALS_ID,
                        params.LABS_NODE_PRIVATE_KEY_PATH,
                        params.LABS_NODE_REMOTE_ROOT
                    )
                }
            }
        }

        stage('Build and archive on labs') {
            agent { label "${params.LABS_NODE_NAME}" }

            environment {
                GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
                ENV_PATH = "${WORKSPACE}/.env"
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
                            ls -l "$ENV_PATH"
                            '''
                        }
                    }
                }

                stage('Resolve Java') {
                    steps {
                        script {
                            env.JAVA_HOME = sh(
                                script: '''#!/usr/bin/env bash
                                set -euo pipefail
                                JAVA_BIN="$(command -v javac || command -v java)"
                                dirname "$(dirname "$(readlink -f "$JAVA_BIN")")"
                                ''',
                                returnStdout: true
                            ).trim()
                            env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
                        }

                        sh 'java -version'
                    }
                }

                stage('Gradle Prep') {
                    steps {
                        sh 'chmod +x gradlew'
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
                        psql -h localhost -U "$MAIN_DB_USER" -p 5435 -d postgres <<'SQL'
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
            }

            post {
                always {
                    archiveArtifacts artifacts: 'app/build/libs/*.jar', allowEmptyArchive: true, fingerprint: true
                    deleteDir()
                }
            }
        }
    }
}
