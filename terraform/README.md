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

3. Authenticate for Terraform (your current lab mode: personal `YC_TOKEN`):

```powershell
$Env:YC_TOKEN = yc iam create-token
```

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

## Auth modes

- Personal account (used in this lab): set `YC_TOKEN` before `plan/apply`.
- Service account: set `service_account_key_file` in `terraform.tfvars` and remove token dependency.
