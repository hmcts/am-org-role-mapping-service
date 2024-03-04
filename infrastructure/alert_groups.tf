data "azurerm_key_vault_secret" "am_prm_support_email" {
  name      = "am-prm-support-email"
  key_vault_id = data.azurerm_key_vault.am_key_vault.id
}

module "am-prm-action-group" {
  source   = "git@github.com:hmcts/cnp-module-action-group"
  location = "global"
  env      = var.env

  resourcegroup_name     = "am-shared-infrastructure-${local.local_env}"
  action_group_name      = "am-prm-support"
  short_name             = "am-prm-support"
  email_receiver_name    = "AM PRM Process Support Mailing List"
  email_receiver_address = data.azurerm_key_vault_secret.am_prm_support_email.value
}