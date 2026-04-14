# Sourced by Jenkinsfile when KUBECONFIG is already set.
# Jenkins agents often lack kubectl in PATH; Minikube installs kubectl via `minikube kubectl --`.
#
# Resolve the real kubectl binary *before* defining a function named kubectl — otherwise
# `command -v kubectl` would match this function and mask "no binary on PATH".

# type -P = only a real binary on PATH (not a shell function named kubectl)
_KUBECTL_EXE=$(type -P kubectl 2>/dev/null || true)

kubectl() {
  if [ -n "${_KUBECTL_EXE}" ]; then
    "${_KUBECTL_EXE}" "$@"
  elif command -v minikube >/dev/null 2>&1; then
    minikube kubectl -- "$@"
  else
    echo "ERROR: neither kubectl nor minikube found in PATH=$PATH" >&2
    exit 127
  fi
}

jenkins_kubectl_diagnostics() {
  echo "=== kubectl / cluster (diagnostics) ==="
  echo "PATH=$PATH"
  echo "KUBECONFIG=${KUBECONFIG:-}"
  if [ -n "${_KUBECTL_EXE}" ]; then
    echo "kubectl binary: ${_KUBECTL_EXE}"
  else
    echo "kubectl binary: not on PATH (will use: minikube kubectl --)"
  fi
  if command -v minikube >/dev/null 2>&1; then
    echo "minikube: $(command -v minikube)"
    minikube status || true
  else
    echo "minikube: not in PATH"
  fi
}
