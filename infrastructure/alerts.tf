variable "process_names" {
  description = "A list of process names"
  type = list(object({
    key = string
    frequencyInMinutes = number
    timeWindowInMinutes = number
    processName = string
  }))
  default = [
    {
      key = "prm-process-1"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 1"
    },
    {
      key = "prm-process-2"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 2"
    },
    {
      key = "prm-process-3"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 3"
    },
    {
      key = "prm-process-4"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 4"
    },
    {
      key = "prm-process-5"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 5"
    },
    {
      key = "prm-process-6"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 6"
    }
  ]
}

variable "action_group_name" {
  description = "The name of the action group"
  type = string
  default = "am-prm-support"
}

module "prm-process-not-started-alerts" {
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  for_each = { for process in var.process_names : process.key => process }

  location = var.location
 
  app_insights_name = join("-", ["am", local.local_env])
 
  alert_name = "am-${each.value.key}-not-started-alert"
  alert_desc = "Triggers when ${each.value.processName} has not started the expected time frame in am-${local.local_env}."
  app_insights_query = "customEvents | where name == '${each.value.processName} - Started' | limit 1"
  custom_email_subject = "Alert: ${each.value.processName} has not started in expected time frame in am-${local.local_env}"
  frequency_in_minutes = each.value.frequencyInMinutes
  time_window_in_minutes = each.value.timeWindowInMinutes
  severity_level = "2"
  action_group_name = var.action_group_name
  trigger_threshold_operator = "LessThan"
  trigger_threshold = "2"
  resourcegroup_name = "am-shared-infrastructure-${local.local_env}"
  common_tags = var.common_tags
  enabled = true
}

module "prm-process-not-completed-alerts" {
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  for_each = { for process in var.process_names : process.key => process }

  location = var.location
 
  app_insights_name = join("-", ["am", local.local_env])
 
  alert_name = "am-${each.value.key}-not-completed-alert"
  alert_desc = "Triggers when ${each.value.processName} has started but not completed am-${local.local_env}."
  app_insights_query = "customEvents | where name startswith '${each.value.processName}' | where timestamp >= ago(60m) | order by timestamp asc | extend prevName = prev(name) | where prevName == '${each.value.processName} - Started' and name == '${each.value.processName}' - Completed'"
  custom_email_subject = "Alert: ${each.value.processName} has not completed am-${local.local_env}"
  frequency_in_minutes = each.value.frequencyInMinutes
  time_window_in_minutes = each.value.timeWindowInMinutes
  severity_level = "2"
  action_group_name = var.action_group_name
  trigger_threshold_operator = "LessThan"
  trigger_threshold = "2"
  resourcegroup_name = "am-shared-infrastructure-${local.local_env}"
  common_tags = var.common_tags
  enabled = true
}

module "prm-process-failure-alerts" {
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  for_each = { for process in var.process_names : process.key => process }

  location = var.location
 
  app_insights_name = join("-", ["am", local.local_env])
 
  alert_name = "am-${each.value.key}-failure-alert"
  alert_desc = "Triggers when ${each.value.processName} fails am-${local.local_env}."
  app_insights_query = "customEvents | where name == '${each.value.processName} - Failed' | where timestamp >= ago(15m) | sort by timestamp desc"
  custom_email_subject = "Alert: ${each.value.processName} failed in am-${var.env}"
  frequency_in_minutes = each.value.frequencyInMinutes
  time_window_in_minutes = each.value.timeWindowInMinutes
  severity_level = "2"
  action_group_name = var.action_group_name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold = "0"
  resourcegroup_name = "am-shared-infrastructure-${local.local_env}"
  common_tags = var.common_tags
  enabled = true
}
