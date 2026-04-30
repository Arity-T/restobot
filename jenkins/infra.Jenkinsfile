pipeline {
    agent { label 'labs' }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    parameters {
        choice(name: 'STACK_ACTION', choices: ['apply', 'delete'], description: 'Create/update or delete OpenStack Heat stack')
        string(name: 'STACK_NAME', defaultValue: 'restobot-stack', description: 'Heat stack name')
        string(name: 'TEMPLATE_PATH', defaultValue: 'heat/restobot-stack.yaml', description: 'Path to Heat template in repository')
        string(name: 'ENV_PATH', defaultValue: 'heat/restobot-stack.env', description: 'Path where Heat env file will be written')
    }

    environment {
        OPENSTACK_RC_PATH = "${WORKSPACE}/openstack.rc"
        HEAT_ENV_PATH = "${WORKSPACE}/${params.ENV_PATH}"
        STACK_OUTPUTS_PATH = "${WORKSPACE}/heat/stack-outputs.txt"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare OpenStack RC') {
            steps {
                withCredentials([file(credentialsId: 'openstack_rc', variable: 'OPENSTACK_RC_FILE')]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail

                    perl -pe 's/\r$//' "$OPENSTACK_RC_FILE" > "$OPENSTACK_RC_PATH"
                    '''
                }
            }
        }

        stage('Prepare Heat Inputs') {
            when {
                expression { params.STACK_ACTION == 'apply' }
            }
            steps {
                withCredentials([
                    file(credentialsId: 'restobot_heat_env', variable: 'HEAT_ENV_FILE'),
                    sshUserPrivateKey(credentialsId: 'restobot_vm_ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')
                ]) {
                    sh '''#!/usr/bin/env bash
                    set -euo pipefail

                    mkdir -p "$(dirname "$HEAT_ENV_PATH")"
                    perl -pe 's/\r$//' "$HEAT_ENV_FILE" > "$HEAT_ENV_PATH"
                    chmod 600 "$SSH_KEY"
                    SSH_PUBLIC_KEY="$(ssh-keygen -y -f "$SSH_KEY")"
                    export SSH_PUBLIC_KEY SSH_USER HEAT_ENV_PATH

                    python3 -c 'import os, pathlib; p = pathlib.Path(os.environ["HEAT_ENV_PATH"]); user = os.environ["SSH_USER"]; key = os.environ["SSH_PUBLIC_KEY"]; lines = p.read_text().splitlines(); out = []; seen_user = False; seen_key = False
for line in lines:
    stripped = line.lstrip()
    indent = line[:len(line) - len(stripped)]
    if stripped.startswith("ssh_user:"):
        out.append(f"{indent}ssh_user: {user}")
        seen_user = True
    elif stripped.startswith("ssh_public_key:"):
        out.append(f"{indent}ssh_public_key: \"{key}\"")
        seen_key = True
    else:
        out.append(line)
if not seen_user:
    out.append(f"  ssh_user: {user}")
if not seen_key:
    out.append(f"  ssh_public_key: \"{key}\"")
p.write_text("\\n".join(out) + "\\n")'
                    '''
                }
            }
        }

        stage('Validate Heat Template') {
            when {
                expression { params.STACK_ACTION == 'apply' }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                . "$OPENSTACK_RC_PATH"
                openstack orchestration template validate --template "$TEMPLATE_PATH"
                '''
            }
        }

        stage('Apply Stack') {
            when {
                expression { params.STACK_ACTION == 'apply' }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                . "$OPENSTACK_RC_PATH"

                if openstack stack show "$STACK_NAME" >/dev/null 2>&1; then
                  set +e
                  UPDATE_OUTPUT="$(openstack stack update --wait --template "$TEMPLATE_PATH" --environment "$HEAT_ENV_PATH" "$STACK_NAME" 2>&1)"
                  UPDATE_STATUS=$?
                  set -e
                  echo "$UPDATE_OUTPUT"

                  if [ "$UPDATE_STATUS" -ne 0 ]; then
                    echo "$UPDATE_OUTPUT" | grep -q 'No updates are to be performed' || exit "$UPDATE_STATUS"
                  fi
                else
                  openstack stack create --wait --template "$TEMPLATE_PATH" --environment "$HEAT_ENV_PATH" "$STACK_NAME"
                fi

                {
                  echo "stack_name=$STACK_NAME"
                  echo "floating_ip=$(openstack stack output show "$STACK_NAME" floating_ip -f value -c output_value)"
                  echo "fixed_ip=$(openstack stack output show "$STACK_NAME" fixed_ip -f value -c output_value)"
                  echo "ssh_user=$(openstack stack output show "$STACK_NAME" ssh_user -f value -c output_value)"
                  echo "server_name=$(openstack stack output show "$STACK_NAME" server_name -f value -c output_value)"
                } > "$STACK_OUTPUTS_PATH"

                cat "$STACK_OUTPUTS_PATH"
                '''
            }
        }

        stage('Delete Stack') {
            when {
                expression { params.STACK_ACTION == 'delete' }
            }
            steps {
                sh '''#!/usr/bin/env bash
                set -euo pipefail
                . "$OPENSTACK_RC_PATH"

                if openstack stack show "$STACK_NAME" >/dev/null 2>&1; then
                  openstack stack delete --yes --wait "$STACK_NAME"
                else
                  echo "Stack $STACK_NAME does not exist. Nothing to delete."
                fi
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'heat/stack-outputs.txt', allowEmptyArchive: true, fingerprint: true
            deleteDir()
        }
    }
}
