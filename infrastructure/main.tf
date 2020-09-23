
locals {
  app_full_name = join("-", [var.product, var.component])
  local_env = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env
  //"${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"

  // Vault name
  previewVaultName = join("-", [var.raw_product, "aat"])//"${var.raw_product}-aat"
  nonPreviewVaultName = join("-", [var.raw_product, var.env])//"${var.raw_product}-${var.env}"
  vaultName = (var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName
  //"${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  // Shared Resource Group
  previewResourceGroup = join("-", [var.raw_product, "shared-infrastructure-aat"])//"${var.raw_product}-shared-infrastructure-aat"
  nonPreviewResourceGroup = join("-", [var.raw_product, "shared-infrastructure", var.env]) //"${var.raw_product}-shared-infrastructure-${var.env}"
  sharedResourceGroup = (var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup
  //"${(var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup}"

}

data "azurerm_key_vault" "am_key_vault" {
  name = local.vaultName
  resource_group_name = local.sharedResourceGroup
}

data "azurerm_key_vault" "s2s_vault" {
  name = join("-", ["s2s", local.local_env])//"s2s-${local.local_env}"
  resource_group_name = join("-", ["rpe-service-auth-provider", local.local_env]) //"rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "microservicekey-am-org-role-mapping-service"
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
}

resource "azurerm_key_vault_secret" "am_org-role-mapping_service_s2s_secret" {
  name = "am-org-role-mapping-service-s2s-secret"
  value = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}


module "org-role-mapping-service-db" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = join("-", [local.app_full_name, "postgres-db"])//"${local.app_full_name}-postgres-db"
  location = var.location
  env = var.env
  subscription = var.subscription
  postgresql_user = var.postgresql_user
  database_name = var.database_name
  storage_mb = var.database_storage_mb
  common_tags = var.common_tags
}

////////////////////////////////
// Populate Vault with DB info//
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = join("-", [var.component, "POSTGRES-USER"])//"${var.component}-POSTGRES-USER"
  value = module.org-role-mapping-service-db.user_name
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = join("-", [var.component, "POSTGRES-PASS"])//"${var.component}-POSTGRES-PASS"
  value = module.org-role-mapping-service-db.postgresql_password
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = join("-", [var.component, "POSTGRES-HOST"])//"${var.component}-POSTGRES-HOST"
  value = module.org-role-mapping-service-db.host_name
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = join("-", [var.component, "POSTGRES-PORT"])//"${var.component}-POSTGRES-PORT"
  value = module.org-role-mapping-service-db.postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = join("-", [var.component, "POSTGRES-DATABASE"])//"${var.component}-POSTGRES-DATABASE"
  value = module.org-role-mapping-service-db.postgresql_database
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}
