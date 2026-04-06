#!/usr/bin/env bash
set -euo pipefail

if [ -z "${KUBECONFIG_PATH:-}" ]; then
  echo "KUBECONFIG_PATH is not set"
  exit 1
fi

KUBECTL_IMAGE="${KUBECTL_IMAGE:-bitnami/kubectl:latest}"

MOUNT_ARGS=()
if [ -f "/.dockerenv" ]; then
  MOUNT_ARGS+=(--volumes-from "${HOSTNAME}")
else
  MOUNT_ARGS+=(-v "$PWD:$PWD")
fi

exec docker run --rm \
  "${MOUNT_ARGS[@]}" \
  -u "$(id -u):$(id -g)" \
  -w "$PWD" \
  -e KUBECONFIG="$KUBECONFIG_PATH" \
  "$KUBECTL_IMAGE" "$@"
