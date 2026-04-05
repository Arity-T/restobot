output "instance_names" {
  description = "Created VM names."
  value       = [for instance in yandex_compute_instance.restobot : instance.name]
}

output "instance_public_ips" {
  description = "Public IPs for SSH/HTTP access."
  value       = [for instance in yandex_compute_instance.restobot : instance.network_interface[0].nat_ip_address]
}

output "instance_private_ips" {
  description = "Private IPs inside VPC subnet."
  value       = [for instance in yandex_compute_instance.restobot : instance.network_interface[0].ip_address]
}

output "application_urls" {
  description = "Expected app URLs."
  value       = [for ip in yandex_compute_instance.restobot[*].network_interface[0].nat_ip_address : "http://${ip}:${var.app_port}"]
}

output "network_id" {
  description = "Managed VPC network ID (null when reusing existing subnet/SG)."
  value       = var.create_network_resources ? yandex_vpc_network.restobot[0].id : null
}

output "subnet_id" {
  description = "Subnet ID used by created VMs."
  value       = local.subnet_id
}

output "security_group_ids" {
  description = "Security group IDs attached to created VMs."
  value       = local.security_group_ids
}

output "ansible_inventory_path" {
  description = "Path to generated Ansible inventory."
  value       = local_file.ansible_inventory.filename
}
