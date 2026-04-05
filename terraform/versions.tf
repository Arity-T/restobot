terraform {
  required_version = ">= 1.6.3"

  required_providers {
    local = {
      source = "hashicorp/local"
    }

    yandex = {
      source = "yandex-cloud/yandex"
    }
  }
}

provider "yandex" {
  cloud_id  = var.cloud_id
  folder_id = var.folder_id
  zone      = var.zone
}
