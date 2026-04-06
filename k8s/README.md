# Kubernetes deployment for lab 7

This directory contains manifests used by Jenkins pipeline for lab 7.

## Resources

- `postgres.yaml` - Postgres Deployment + Service.
- `db-init-job.yaml` - migration Job (runs SQL from ConfigMap).
- `app-deployment.yaml` - RestoBot Deployment template with image placeholders.
- `app-service.yaml` - NodePort Service (`30089`).

## Jenkins credentials

- `restobot_env` - Secret file with application environment.
- `restobot_kubeconfig` - Secret file with kubeconfig for target cluster.
- `docker_registry` - Username/Password (optional, only if `PUSH_IMAGE=true`).

## Pipeline parameters

- `K8S_ACTION=deploy|delete`
- `RUN_BUILD=true|false`
- `PUSH_IMAGE=true|false`
- `IMAGE_REPOSITORY` (for example `restobot-app` or `your-dockerhub/restobot-app`)
- `IMAGE_TAG` (empty = `build-<BUILD_NUMBER>`)
- `K8S_NAMESPACE`
- `DELETE_NAMESPACE` (used with `K8S_ACTION=delete`)

## Local access check (Docker Desktop Kubernetes)

```bash
kubectl -n restobot get svc restobot-app
# Use the assigned nodePort from output:
curl http://localhost:<NODE_PORT>/healthcheck
```
