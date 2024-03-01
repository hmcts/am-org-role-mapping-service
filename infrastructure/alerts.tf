variable "process_names" {
  description = "A list of process names"
  type = list(object({
    key         = string
    description = string
    notStartedQueryFilter = string
    processName = string
  }))
  default = [
    {
      key                   = "prm-process1"
      description           = "PRM Process 1"
      notStartedQueryFilter = "Process 1 - Started",
      processName = "Process 1"
    },
    {
      key                   = "prm-process2"
      description           = "PRM Process 2",
      notStartedQueryFilter = "Process 2 - Started",
      processName = "Process 2"
    },
    {
      key                   = "prm-process3"
      description           = "PRM Process 3",
      notStartedQueryFilter = "Process 3 - Started",
      processName = "Process 3"
    },
    {
      key                   = "prm-process4"
      description           = "PRM Process 4",
      notStartedQueryFilter = "Process 4 - Started",
      processName = "Process 4"
    },
    {
      key                   = "prm-process5"
      description           = "PRM Process 5",
      notStartedQueryFilter = "Process 5 - Started",
      processName = "Process 5"
    },
    {
      key                   = "prm-process6"
      description           = "PRM Process 6",
      notStartedQueryFilter = "Process 6 - Started",
      processName = "Process 6"
    }
  ]
}

module "prm-process-not-started-alerts" {
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  for_each = { for process in var.process_names : process.key => process }

  location = var.location
 
  app_insights_name = join("-", ["am", local.local_env])
 
  alert_name = "am-${each.value.key}-not-started-alert"
  alert_desc = "Triggers when ${each.value.description} has not started within the past hour in am-${local.local_env}."
  app_insights_query = "customEvents | where name == '${each.value.notStartedQueryFilter}' | limit 1"
  custom_email_subject = "Alert: ${each.value.description} has not started in expected time frame in am-${local.local_env}"
  frequency_in_minutes = "15"
  time_window_in_minutes = "1440"
  severity_level = "2"
  action_group_name = "am-support"
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
  alert_desc = "Triggers when ${each.value.description} has started but not completed am-${local.local_env}."
  app_insights_query = "customEvents | where name startswith '${each.value.processName}' | where timestamp >= ago(60m) | order by timestamp asc | extend prevName = prev(name) | where prevName == '${each.value.processName} - Started' and name == '${each.value.processName}' - Completed'"
  custom_email_subject = "Alert: ${each.value.description} has not completed am-${local.local_env}"
  frequency_in_minutes = "15"
  time_window_in_minutes = "15"
  severity_level = "2"
  action_group_name = "am-support"
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
  alert_desc = "Triggers when ${each.value.description} fails am-${local.local_env}."
  app_insights_query = "customEvents | where name == '${each.value.processName} - Failed' | where timestamp >= ago(15m) | sort by timestamp desc"
  custom_email_subject = "Alert: ${each.value.description} failed in am-${var.env}"
  frequency_in_minutes = "15"
  time_window_in_minutes = "15"
  severity_level = "2"
  action_group_name = "am-support"
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold = "0"
  resourcegroup_name = "am-shared-infrastructure-${local.local_env}"
  common_tags = var.common_tags
  enabled = true
}
