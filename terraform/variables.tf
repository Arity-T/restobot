variable "service_account_key_file" {
  description = "Path to the Yandex Cloud service account JSON key file."
  type        = string
  sensitive   = true
}

variable "cloud_id" {
  description = "Yandex Cloud ID."
  type        = string
}

variable "folder_id" {
  description = "Yandex Cloud folder ID."
  type        = string
}

variable "zone" {
  description = "Default availability zone."
  type        = string
  default     = "ru-central1-a"
}

variable "project_name" {
  description = "Resource name prefix."
  type        = string
  default     = "restobot"
}

variable "instance_count" {
  description = "How many VMs to create."
  type        = number
  default     = 1

  validation {
    condition     = var.instance_count >= 1
    error_message = "instance_count must be at least 1."
  }
}

variable "create_network_resources" {
  description = "When true, Terraform creates VPC network/subnet/security group. When false, it reuses existing subnet and security groups."
  type        = bool
  default     = true
}

variable "existing_subnet_id" {
  description = "Existing subnet ID to use when create_network_resources is false."
  type        = string
  default     = ""
}

variable "existing_security_group_ids" {
  description = "Existing security group IDs to attach to VM when create_network_resources is false."
  type        = list(string)
  default     = []
}

variable "subnet_cidr" {
  description = "CIDR block for the subnet."
  type        = string
  default     = "10.10.10.0/24"
}

variable "allowed_cidrs" {
  description = "CIDRs allowed for SSH and app port access."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "platform_id" {
  description = "Yandex Compute platform ID."
  type        = string
  default     = "standard-v3"
}

variable "vm_cores" {
  description = "vCPU count per VM."
  type        = number
  default     = 2
}

variable "vm_memory_gb" {
  description = "RAM in GB per VM."
  type        = number
  default     = 2
}

variable "vm_core_fraction" {
  description = "Guaranteed vCPU percentage."
  type        = number
  default     = 100
}

variable "boot_disk_size_gb" {
  description = "Boot disk size in GB."
  type        = number
  default     = 20
}

variable "boot_disk_type" {
  description = "Boot disk type."
  type        = string
  default     = "network-hdd"
}

variable "preemptible" {
  description = "Create preemptible VMs for lower cost."
  type        = bool
  default     = true
}

variable "image_family" {
  description = "Image family used for VM boot disk."
  type        = string
  default     = "ubuntu-2204-lts"
}

variable "ssh_user" {
  description = "Administrative SSH user created via cloud-init."
  type        = string
  default     = "restobot"
}

variable "ssh_public_key_path" {
  description = "Path to SSH public key to inject into VM metadata."
  type        = string
}

variable "app_port" {
  description = "Application port to open in security group."
  type        = number
  default     = 8089
}
