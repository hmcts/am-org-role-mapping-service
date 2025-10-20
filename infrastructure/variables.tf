variable "product" {
  type = string
}

variable "raw_product" {
  type    = string
  default = "am"
  // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
  type = string
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "env" {
  type = string
}

variable "subscription" {
  type = string
}

variable "common_tags" {
  type = map(string)
}

////////////////////////////////
// Alerts
////////////////////////////////

variable "enable_prm_process_not_started_alerts" {
  type        = bool
  default     = false
  description = "Enables the alerts for processes not started. Defaults to false"
}

variable "enable_prm_process_not_completed_alerts" {
  type        = bool
  default     = false
  description = "Enables the alerts for processes not completed. Defaults to false"
}

variable "enable_prm_process_failure_alerts" {
  type        = bool
  default     = false
  description = "Enables the alerts for processes with failures. Defaults to false"
}

////////////////////////////////
// Database
////////////////////////////////

variable "postgresql_user" {
  type    = string
  default = "am"
}

variable "database_name" {
  type    = string
  default = "org_role_mapping"
}

variable "database_sku_name" {
  type    = string
  default = "GP_Gen5_2"
}

variable "database_sku_capacity" {
  default = "2"
}

variable "database_storage_mb" {
  default = "51200"
}

variable "aks_subscription_id" {
}

variable "jenkins_AAD_objectId" {
  type        = string
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "enable_schema_ownership" {
  type        = bool
  default     = false
  description = "Enables the schema ownership script. Change this to true if you want to use the script. Defaults to false"
}

variable "force_schema_ownership_trigger" {
  default     = ""
  type        = string
  description = "Update this to a new value to force the schema ownership script to run again."
}

variable "kv_subscription" {
  default     = "DCD-CNP-DEV"
  type        = string
  description = "Update this with the name of the subscription where the single server key vault is. Defaults to DCD-CNP-DEV."
}

variable "pgsql_sku" {
  description = "The PGSql flexible server instance sku"
  default     = "GP_Standard_D2s_v3"
}

variable "action_group_name" {
  description = "The name of the Action Group to create."
  type        = string
  default     = "action_group"
}

variable "email_address_key" {
  description = "Email address key in azure Key Vault."
  type        = string
  default     = "db-alert-monitoring-email-address"
}

variable "email_address_key_vault_id" {
  description = "Email address Key Vault Id."
  type        = string
  default     = ""
}