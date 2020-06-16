variable "product" {
  type = string
}

variable "raw_product" {
  default = "am"
  // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
  type = string
}

variable "location" {
  type = string
  default = "UK South"
}

variable "env" {
  type = string
}

variable "subscription" {
  type = string
}

variable "ilbIp" {}

variable "common_tags" {
  type = map
}

variable "appinsights_instrumentation_key" {
  default = ""
}

variable "root_logging_level" {
  default = "INFO"
}

variable "log_level_spring_web" {
  default = "INFO"
}

variable "team_name" {
  default = "AM"
}

variable "managed_identity_object_id" {
  default = ""
}

variable "enable_ase" {
  default = false
}

variable "authorised-services" {
  type = string
  default = "ccd_gw,am_org-role-mapping_service"
}


variable "deployment_namespace" {}

////////////////////////////////
// Database
////////////////////////////////

variable "postgresql_user" {
  default = "am"
}

variable "database_name" {
  default = "org_role_mapping"
}

variable "data_store_max_pool_size" {
  default = "16"
}

variable "database_sku_name" {
  default = "GP_Gen5_2"
}

variable "database_storage_mb" {
  default = "51200"
}

