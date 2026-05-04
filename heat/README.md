# Heat for labs 3 and 4

This directory contains the OpenStack Heat template for provisioning a single
VM used for application deployment.

## Files

- `restobot-stack.yaml` - HOT template for server, port, security group and floating IP.
- `restobot-stack.env.example` - example parameter file.
- `cloud-init.yaml` - cloud-init used by Heat to create the SSH/deploy user.

## Required OpenStack inputs

- existing private `network`
- existing `subnet`
- external network for floating IP
- existing OpenStack keypair (`key_name`)
- public SSH key for the deploy user (`ssh_public_key`)

## Example CLI usage

```bash
source openstack.rc
cp heat/restobot-stack.env.example heat/restobot-stack.env

openstack stack create \
  --wait \
  --template heat/restobot-stack.yaml \
  --environment heat/restobot-stack.env \
  gaar-restobot-stack

openstack stack output show gaar-restobot-stack floating_ip -f value -c output_value
```

