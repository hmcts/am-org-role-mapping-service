locals {
  app_full_name = join("-", [var.product, var.component])

  local_env = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env

  // Vault name
  previewVaultName    = join("-", [var.raw_product, "aat"])
  nonPreviewVaultName = join("-", [var.raw_product, var.env])
  vaultName           = (var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName

  // Shared Resource Group
  previewResourceGroup    = join("-", [var.raw_product, "shared-infrastructure-aat"])
  nonPreviewResourceGroup = join("-", [var.raw_product, "shared-infrastructure", var.env])
  sharedResourceGroup     = (var.env == "preview" || var.env == "spreview") ? local.previewResourceGroup : local.nonPreviewResourceGroup
  dbName = join("-", [local.app_full_name, "postgres-db", "v15"])
}

data "azurerm_key_vault" "am_key_vault" {
  name                = local.vaultName
  resource_group_name = local.sharedResourceGroup
}

data "azurerm_key_vault" "s2s_vault" {
  name                = join("-", ["s2s", local.local_env])
  resource_group_name = join("-", ["rpe-service-auth-provider", local.local_env])
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name         = "microservicekey-am-org-role-mapping-service"
  key_vault_id = data.azurerm_key_vault.s2s_vault.id
}

resource "azurerm_key_vault_secret" "am_org-role-mapping_service_s2s_secret" {
  name         = "am-org-role-mapping-service-s2s-secret"
  value        = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name         = join("-", [var.component, "POSTGRES-PASS"])
  value        = module.org-role-mapping-database-v15.password
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name         = join("-", [var.component, "POSTGRES-USER"])
  value        = module.org-role-mapping-database-v15.username
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name         = join("-", [var.component, "POSTGRES-HOST"])
  value        = module.org-role-mapping-database-v15.fqdn
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name         = join("-", [var.component, "POSTGRES-PORT"])
  value        = "5432"
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name         = join("-", [var.component, "POSTGRES-DATABASE"])
  value        = var.database_name
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

module "org-role-mapping-database-v15" {
  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"

  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  admin_user_object_id = var.jenkins_AAD_objectId
  business_area        = "cft"
  name                 = local.dbName
  product              = var.product
  env                  = var.env
  component            = var.component
  common_tags          = var.common_tags
  pgsql_version        = "15"
  pgsql_sku            = var.pgsql_sku

  # Setup Access Reader db user
  force_user_permissions_trigger = "5"

  # Sets correct DB owner after migration to fix permissions
  enable_schema_ownership        = var.enable_schema_ownership
  force_schema_ownership_trigger = "5"
  kv_subscription                = var.kv_subscription
  kv_name                        = data.azurerm_key_vault.am_key_vault.name
  user_secret_name               = azurerm_key_vault_secret.POSTGRES-USER.name
  pass_secret_name               = azurerm_key_vault_secret.POSTGRES-PASS.name

  # The original subnet is full, this is required to use the new subnet for new databases
  subnet_suffix = "expanded"

  action_group_name = join("-", [local.dbName, var.action_group_name])
  email_address_key = var.email_address_key
  email_address_key_vault_id = data.azurerm_key_vault.am_key_vault.id

  pgsql_databases = [
    {
      name = var.database_name
    }
  ]

  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "pg_stat_statements,pg_buffercache"
    },
    {
      name  = "azure.enable_temp_tablespaces_on_local_ssd"
      value = "off"
    }
  ]
}
