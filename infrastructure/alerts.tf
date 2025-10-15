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
      processName           = "PRM Process 1 - Find Case Definition Changes"
    },
    {
      key = "prm-process-2"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 2 - Find Organisations with Stale Profiles"
    },
    {
      key = "prm-process-3"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 3 - Find organisation changes"
    },
    {
      key = "prm-process-4"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 4 - Find Users with Stale Organisations"
    },
    {
      key = "prm-process-5"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 5 - Find User Changes"
    },
    {
      key = "prm-process-6"
      frequencyInMinutes    = 15
      timeWindowInMinutes   = 10080
      processName           = "PRM Process 6 - Refresh - Single User Mode and new PRM endpoint"
    }
  ]
}

module "prm-process-not-started-alerts" {
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  for_each = { for process in var.process_names : process.key => process }

  location = var.location
 
  app_insights_name = join("-", ["am", local.local_env])
 
  alert_name = "am-${each.value.key}-not-started-alert"
  alert_desc = "Triggers when ${each.value.processName} has not started the expected time frame in am-${local.local_env}."
  app_insights_query = "customEvents | where cloud_RoleName = 'am-org-role-mapping-service' and cloud_RoleInstance startswith 'am-org-role-mapping-service-java' | where name == '${each.value.processName} - Started' | limit 1"
  custom_email_subject = "Alert: ${each.value.processName} has not started in expected time frame in am-${local.local_env}"
  frequency_in_minutes = each.value.frequencyInMinutes
  time_window_in_minutes = each.value.timeWindowInMinutes
  severity_level = "2"
  action_group_name = module.am-prm-action-group.action_group_name
  trigger_threshold_operator = "LessThan"
  trigger_threshold = "1"
  resourcegroup_name = local.sharedResourceGroup
  common_tags = var.common_tags
  enabled = var.enable_prm_process_not_started_alerts
}

module "prm-process-not-completed-alerts" {
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  for_each = { for process in var.process_names : process.key => process }

  location = var.location
 
  app_insights_name = join("-", ["am", local.local_env])
 
  alert_name = "am-${each.value.key}-not-completed-alert"
  alert_desc = "Triggers when ${each.value.processName} has started but not completed am-${local.local_env}."
  app_insights_query = "customEvents | where cloud_RoleName = 'am-org-role-mapping-service' and cloud_RoleInstance startswith 'am-org-role-mapping-service-java' | where name startswith '${each.value.processName}' | where timestamp >= ago(60m) | order by timestamp asc | extend prevName = prev(name) | where prevName == '${each.value.processName} - Started' and name == '${each.value.processName}' - Completed'"
  custom_email_subject = "Alert: ${each.value.processName} has not completed am-${local.local_env}"
  frequency_in_minutes = each.value.frequencyInMinutes
  time_window_in_minutes = each.value.timeWindowInMinutes
  severity_level = "2"
  action_group_name = module.am-prm-action-group.action_group_name
  trigger_threshold_operator = "LessThan"
  trigger_threshold = "1"
  resourcegroup_name = local.sharedResourceGroup
  common_tags = var.common_tags
  enabled = var.enable_prm_process_not_completed_alerts
}

module "prm-process-failure-alerts" {
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  for_each = { for process in var.process_names : process.key => process }

  location = var.location
 
  app_insights_name = join("-", ["am", local.local_env])
 
  alert_name = "am-${each.value.key}-failure-alert"
  alert_desc = "Triggers when ${each.value.processName} fails am-${local.local_env}."
  app_insights_query = "customEvents | where cloud_RoleName = 'am-org-role-mapping-service' and cloud_RoleInstance startswith 'am-org-role-mapping-service-java' | where name == '${each.value.processName} - Failed'"
  custom_email_subject = "Alert: ${each.value.processName} failed in am-${var.env}"
  frequency_in_minutes = each.value.frequencyInMinutes
  time_window_in_minutes = each.value.timeWindowInMinutes
  severity_level = "2"
  action_group_name = module.am-prm-action-group.action_group_name
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold = "0"
  resourcegroup_name = local.sharedResourceGroup
  common_tags = var.common_tags
  enabled = var.enable_prm_process_failure_alerts
}
