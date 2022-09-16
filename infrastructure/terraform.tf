provider "azurerm" {
  features {}
}

terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source = "hashicorp/azurerm"
      version = "~> 3.23.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.26.1"
    }
  }
}