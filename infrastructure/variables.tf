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

variable "ilbIp" {
  type = string
}

variable "common_tags" {
  type =  map(string)
}

variable "appinsights_instrumentation_key" {
  type    = string
  default = ""
}

variable "root_logging_level" {
  type    = string
  default = "INFO"
}

variable "log_level_spring_web" {
  type    = string
  default = "INFO"
}

variable "team_name" {
  type    = string
  default = "AM"
}

variable "managed_identity_object_id" {
  type    = string
  default = ""
}

variable "enable_ase" {
  type    = bool
  default = false
}

variable "deployment_namespace" {
  type = string
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

variable "data_store_max_pool_size" {
  type    = string
  default = "16"
}

variable "database_sku_name" {
  type    = string
  default = "GP_Gen5_2"
}

variable "database_sku_capacity" {
  type    = string
  default = "2"
}

variable "database_storage_mb" {
  type    = string
  default = "51200"
}