locals {
  ssh_public_key = trimspace(file(var.ssh_public_key_path))
}

data "yandex_compute_image" "ubuntu" {
  family = var.image_family
}

resource "yandex_vpc_network" "restobot" {
  name = "${var.project_name}-network"
}

resource "yandex_vpc_subnet" "restobot" {
  name           = "${var.project_name}-subnet"
  zone           = var.zone
  network_id     = yandex_vpc_network.restobot.id
  v4_cidr_blocks = [var.subnet_cidr]
}

resource "yandex_vpc_security_group" "restobot" {
  name        = "${var.project_name}-sg"
  description = "Security group for ${var.project_name}"
  network_id  = yandex_vpc_network.restobot.id

  ingress {
    protocol       = "TCP"
    description    = "SSH access"
    v4_cidr_blocks = var.allowed_cidrs
    port           = 22
  }

  ingress {
    protocol       = "TCP"
    description    = "RestoBot application port"
    v4_cidr_blocks = var.allowed_cidrs
    port           = var.app_port
  }

  egress {
    protocol       = "ANY"
    description    = "Allow all outbound traffic"
    v4_cidr_blocks = ["0.0.0.0/0"]
    from_port      = 0
    to_port        = 65535
  }
}

resource "yandex_compute_instance" "restobot" {
  count                     = var.instance_count
  name                      = format("%s-%02d", var.project_name, count.index + 1)
  zone                      = var.zone
  platform_id               = var.platform_id
  allow_stopping_for_update = true

  resources {
    cores         = var.vm_cores
    memory        = var.vm_memory_gb
    core_fraction = var.vm_core_fraction
  }

  scheduling_policy {
    preemptible = var.preemptible
  }

  boot_disk {
    initialize_params {
      image_id = data.yandex_compute_image.ubuntu.id
      size     = var.boot_disk_size_gb
      type     = var.boot_disk_type
    }
  }

  network_interface {
    subnet_id          = yandex_vpc_subnet.restobot.id
    nat                = true
    security_group_ids = [yandex_vpc_security_group.restobot.id]
  }

  metadata = {
    serial-port-enable = 1
    user-data = templatefile("${path.module}/templates/cloud-init.yaml.tftpl", {
      ssh_user       = var.ssh_user
      ssh_public_key = local.ssh_public_key
    })
  }

  lifecycle {
    ignore_changes = [boot_disk[0].initialize_params[0].image_id]
  }
}

resource "local_file" "ansible_inventory" {
  filename = "${path.module}/../ansible/inventory/hosts.ini"
  content = templatefile("${path.module}/templates/hosts.ini.tftpl", {
    ssh_user = var.ssh_user
    hosts = [
      for instance in yandex_compute_instance.restobot : {
        name       = instance.name
        public_ip  = instance.network_interface[0].nat_ip_address
        private_ip = instance.network_interface[0].ip_address
      }
    ]
  })
}
