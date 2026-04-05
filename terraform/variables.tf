variable "cloud_id" {
  description = "Yandex Cloud cloud ID."
  type        = string
}

variable "folder_id" {
  description = "Yandex Cloud folder ID where resources will be created."
  type        = string
}

variable "zone" {
  description = "Availability zone for the subnet and virtual machines."
  type        = string
  default     = "ru-central1-a"
}

variable "project_name" {
  description = "Prefix for created resources."
  type        = string
  default     = "restobot"
}

variable "existing_network_id" {
  description = "Optional ID of an existing Yandex Cloud VPC network. If set, Terraform will reuse it instead of creating a new network."
  type        = string
  default     = null
}

variable "existing_subnet_id" {
  description = "Optional ID of an existing Yandex Cloud subnet. If set, Terraform will reuse it instead of creating a new network and subnet."
  type        = string
  default     = null
}

variable "instance_count" {
  description = "Number of virtual machines to create."
  type        = number
  default     = 1

  validation {
    condition     = var.instance_count >= 1
    error_message = "instance_count must be at least 1."
  }
}

variable "subnet_cidr" {
  description = "CIDR block for the subnet."
  type        = string
  default     = "10.10.10.0/24"
}

variable "allowed_cidrs" {
  description = "CIDR blocks that may access SSH and the application port."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "platform_id" {
  description = "Yandex Cloud VM platform ID."
  type        = string
  default     = "standard-v3"
}

variable "vm_cores" {
  description = "Number of vCPUs per VM."
  type        = number
  default     = 2
}

variable "vm_memory_gb" {
  description = "RAM size in GB per VM."
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
  description = "Whether to create preemptible VMs."
  type        = bool
  default     = true
}

variable "image_family" {
  description = "Image family used for boot disk creation."
  type        = string
  default     = "ubuntu-2204-lts"
}

variable "ssh_user" {
  description = "Administrative user created on the VM for Ansible and SSH access."
  type        = string
  default     = "restobot"
}

variable "ssh_public_key_path" {
  description = "Path to the SSH public key that will be injected into the VM."
  type        = string
}

variable "app_port" {
  description = "Published application port."
  type        = number
  default     = 8089
}
