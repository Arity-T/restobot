output "instance_names" {
  description = "Created virtual machine names."
  value       = [for instance in yandex_compute_instance.restobot : instance.name]
}

output "network_id" {
  description = "Network ID used by the infrastructure."
  value       = local.network_id
}

output "subnet_id" {
  description = "Subnet ID used by the infrastructure."
  value       = local.subnet_id
}

output "uses_existing_network" {
  description = "Whether Terraform reused an existing network or subnet."
  value       = var.existing_network_id != null || var.existing_subnet_id != null
}

output "instance_public_ips" {
  description = "Public IP addresses of created VMs."
  value       = [for instance in yandex_compute_instance.restobot : instance.network_interface[0].nat_ip_address]
}

output "instance_private_ips" {
  description = "Private IP addresses of created VMs."
  value       = [for instance in yandex_compute_instance.restobot : instance.network_interface[0].ip_address]
}

output "application_urls" {
  description = "URLs where the application should be available."
  value       = [for ip in yandex_compute_instance.restobot[*].network_interface[0].nat_ip_address : "http://${ip}:${var.app_port}"]
}

output "ansible_inventory_path" {
  description = "Path to the generated Ansible inventory."
  value       = local_file.ansible_inventory.filename
}
