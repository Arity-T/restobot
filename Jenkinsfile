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
        JAVA_HOME = "/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home"
        PATH = "${JAVA_HOME}/bin:${PATH}"
        ENV_PATH = "${WORKSPACE}/.env"
    }

    stages {
        stage('Checkout') {
            steps {
                // Get the latest code from the repository
                checkout scm
            }
        }

        stage('Prepare .env') {
            steps {
                withCredentials([file(credentialsId: 'restobot_env', variable: 'ENV_FILE')]) {
                    sh '''set -e
                    # Normalize line endings to LF to avoid `/bin/sh` parse issues
                    perl -pe 's/\r$//' "$ENV_FILE" > "$ENV_PATH"
                    ls -l "$ENV_PATH"
                    '''
                }
            }
        }

        stage('Gradle Prep') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Create DB') {
            steps {
                sh '''set -e
                    ENV_PATH="${ENV_PATH:-${WORKSPACE:-$PWD}/.env}"
                    if [ ! -f "$ENV_PATH" ]; then
                    echo "ERROR: $ENV_PATH not found. Make sure the credential 'restobot_env' is configured."
                    exit 1
                    fi
                    set -a
                    . "$ENV_PATH"
                    set +a
                    export PGPASSWORD="$MAIN_DB_PASSWORD"
                    psql -h localhost -U postgres -p 5432 -d postgres <<'SQL'
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

        stage('Verify Java DB Connection') {
            steps {
                sh '''set -e
                    ENV_PATH="${ENV_PATH:-${WORKSPACE:-$PWD}/.env}"
                    set -a
                    . "$ENV_PATH"
                    set +a

                    unset http_proxy HTTP_PROXY https_proxy HTTPS_PROXY all_proxy ALL_PROXY no_proxy NO_PROXY
                    unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS GRADLE_OPTS JAVA_OPTS

                    tmp_dir="$(mktemp -d)"
                    trap 'rm -rf "$tmp_dir"' EXIT

                    cat > "$tmp_dir/JenkinsDbCheck.java" <<'JAVA'
import java.net.InetAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JenkinsDbCheck {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("MAIN_DB_URL");
        Matcher matcher = Pattern.compile("^jdbc:postgresql://([^:/?]+):(\\d+)/.*").matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot parse MAIN_DB_URL=" + url);
        }

        String host = matcher.group(1);
        int port = Integer.parseInt(matcher.group(2));

        System.out.println("Java resolves DB host as " + InetAddress.getByName(host));
        try (Socket ignored = new Socket(host, port)) {
            System.out.println("Java can open DB socket " + host + ":" + port);
        }
    }
}
JAVA
                    javac "$tmp_dir/JenkinsDbCheck.java"
                    java -Djava.net.useSystemProxies=false -Djava.net.preferIPv4Stack=true -cp "$tmp_dir" JenkinsDbCheck
                    '''
            }
        }

        stage('Run Migrations') {
            steps {
                sh '''set -e
                    unset http_proxy HTTP_PROXY https_proxy HTTPS_PROXY all_proxy ALL_PROXY no_proxy NO_PROXY
                    unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS GRADLE_OPTS JAVA_OPTS
                    ./gradlew --no-daemon -Djava.net.useSystemProxies=false -Djava.net.preferIPv4Stack=true :logic:flywayMigrate
                    '''
            }
        }

        stage('Generate jOOQ') {
            steps {
                sh '''set -e
                    unset http_proxy HTTP_PROXY https_proxy HTTPS_PROXY all_proxy ALL_PROXY no_proxy NO_PROXY
                    unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS GRADLE_OPTS JAVA_OPTS
                    ./gradlew --no-daemon -Djava.net.useSystemProxies=false -Djava.net.preferIPv4Stack=true :logic:generateJooq
                    '''
            }
        }

        stage('Build') {
            steps {
                sh '''set -e
                    unset http_proxy HTTP_PROXY https_proxy HTTPS_PROXY all_proxy ALL_PROXY no_proxy NO_PROXY
                    unset JAVA_TOOL_OPTIONS _JAVA_OPTIONS GRADLE_OPTS JAVA_OPTS
                    ./gradlew --no-daemon -Djava.net.useSystemProxies=false -Djava.net.preferIPv4Stack=true build
                    '''
            }
        }
    }


    post {
        always {
            // Always clean workspace to avoid leftover files between builds
            cleanWs()
        }
    }
}
