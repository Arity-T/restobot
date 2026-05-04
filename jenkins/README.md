# Jenkins jobs for labs 3 and 4

This directory contains separate pipeline definitions for infrastructure and
deployment jobs. The build job from lab 2 remains the root `Jenkinsfile`.

## Jobs

- Lab 2 build job:
  - script path: `Jenkinsfile`
- Lab 3 infra job:
  - script path: `jenkins/infra.Jenkinsfile`
- Lab 4 deploy job:
  - script path: `jenkins/deploy.Jenkinsfile`

For moving Jenkins to another PC, see `jenkins/portable-jenkins.md`.

## Current demo defaults

- Jenkins node name/label: `labs`
- Jenkins node host: `192.168.24.19`
- Heat stack name: `gaar-restobot-stack`
- Heat VM name: `gaar-restobot-vm`
- Current deployment target IP: `185.216.204.165`

## Required Jenkins credentials

- `restobot_env` - Secret file with application `.env`
- `openstack_rc` - Secret file with OpenStack CLI environment exports
- `openstack_password` - Secret text with OpenStack password for non-interactive Jenkins jobs
- `restobot_heat_env` - Secret file with Heat parameters file based on `heat/restobot-stack.env.example`
- `restobot_vm_ssh` - SSH Username with private key for VM access

## Jenkins node prerequisites

- Build job:
  - Java 23
  - PostgreSQL client (`psql`) and access to the build database
- Infra job:
  - OpenStack CLI installed and configured through `openstack_rc`
- Deploy job:
  - `ssh`, `scp`, `curl`
  - Copy Artifact plugin for `copyArtifacts` step
  - if Copy Artifact uses project-level permissions, allow deploy job to copy from the lab 2 build job

## Expected workflow

1. Run build job from lab 2 and archive `app/build/libs/app-fat.jar`.
2. Run infra job:
   - `STACK_ACTION=apply`
   - `STACK_NAME=gaar-restobot-stack`
   - creates or updates Heat stack
   - archives stack outputs with floating IP
3. Prepare VM manually following `deploy/manual-vm-setup.md`.
4. Run deploy job:
   - copies archived fat JAR from build job
   - uploads `.env` and systemd unit
   - restarts service with `systemctl`
   - checks `/healthcheck`
