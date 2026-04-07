# Terraform (Lab 5)

## What this creates

- VPC network and subnet
- Security group with SSH (`22`) and app port (`8089`)
- One or more Ubuntu VMs with public IP
- Cloud-init bootstrap (`python3`, sudo user, SSH key injection)
- Generated Ansible inventory: `ansible/inventory/hosts.ini`

## Quick start

1. Copy and fill variables:

```bash
cp terraform.tfvars.example terraform.tfvars
```

2. Set network mode in `terraform.tfvars`:

- Shared/default lab network: `create_network_resources = false`, set `existing_subnet_id` and `existing_security_group_ids`.
- Isolated mode: `create_network_resources = true` to create your own VPC network, subnet, and SG.

3. Put your service account JSON key on disk (e.g. `authorized_key.json` next to the `.tf` files) and set `service_account_key_file` in `terraform.tfvars`.

4. Optional: configure remote backend in Object Storage:

```bash
cp backend.tf.example backend.tf
```

5. Run:

```bash
terraform init
terraform plan
terraform apply
```

6. Destroy when done:

```bash
terraform destroy
```

## Authentication

Terraform uses the service account key file from `service_account_key_file` in `terraform.tfvars` (JSON key for the SA that has rights to manage resources in the target folder).

## Jenkins CI

The root [`Jenkinsfile`](../Jenkinsfile) copies secrets into the workspace before Terraform runs:

| Credential ID | Type | Role |
|---------------|------|------|
| `restobot_env` | Secret file | Application `.env` |
| `restobot_tfvars` | Secret file | `terraform.tfvars` (must set `service_account_key_file = "./authorized_key.json"`) |
| `restobot_yc_sa_key` | Secret file | Same JSON as local `authorized_key.json` (written to `terraform/authorized_key.json`) |
| `restobot_vm_ssh_pub` | Secret file | SSH **public** key (one line, `.pub`) — copied to `terraform/ci_id_ed25519.pub` for cloud-init; must pair with `restobot_vm_ssh` |
| `restobot_vm_ssh` | SSH Username with private key | Private half of `restobot_vm_ssh_pub`. The **Username** field in Jenkins is ignored by the pipeline: Ansible uses `ansible_user` from generated `hosts.ini` (Terraform `ssh_user`, default `restobot`). |

The obsolete `yc_iam_token` credential is not used.
