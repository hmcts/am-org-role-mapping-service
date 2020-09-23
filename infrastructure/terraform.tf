provider "azurerm" {
  features {}
}

terraform {
  required_version = ">= 0.13"  # Terraform client version

  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 2.25"       # AzureRM provider version
    }
  }
}