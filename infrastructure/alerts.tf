variable "process_names" {
  description = "A list of process names"
  type = list(object({
    key         = string
    description = string
    notStartedQueryFilter = string
  }))
  default = [
    {
      key                   = "prm-process1"
      description           = "PRM Process 1"
      notStartedQueryFilter = "Process 1 - Started"
    },
    {
      key                   = "prm-process2"
      description           = "PRM Process 2",
      notStartedQueryFilter = "Process 2 - Started"
    },
    {
      key                   = "prm-process3"
      description           = "PRM Process 3",
      notStartedQueryFilter = "Process 3 - Started"
    },
    {
      key                   = "prm-process4"
      description           = "PRM Process 4",
      notStartedQueryFilter = "Process 4 - Started"
    },
    {
      key                   = "prm-process5"
      description           = "PRM Process 5",
      notStartedQueryFilter = "Process 5 - Started"
    },
    {
      key                   = "prm-process6"
      description           = "PRM Process 6",
      notStartedQueryFilter = "Process 6 - Started"
    }
  ]
}


locals {
  local_env = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env
}

module "prm-process-not-started-alerts" {
  source = "git@github.com:hmcts/cnp-module-metric-alert"

  for_each = { for process in var.process_names : process.key => process }

  location = var.location
 
  app_insights_name = join("-", ["am", local.local_env])
 
  alert_name = "am-${each.value.key}-not-started-alert"
  alert_desc = "Triggers when ${each.value.description} has not started within the past hour in am-${local.local_env}."
  app_insights_query = "customEvents | where name == \"${each.value.notStartedQueryFilter}\" | limit 1"
  custom_email_subject = "Alert: ${each.value.description} has not started in expected time frame in am-${local.local_env}"
  frequency_in_minutes = "15"
  time_window_in_minutes = "1440"
  severity_level = "2"
  action_group_name = "am-support" // or whatever the correct support group should be
  trigger_threshold_operator = "LessThan"
  trigger_threshold = "2"
  resourcegroup_name = "am-shared-infrastructure-${local.local_env}"
  common_tags = var.common_tags
  enabled = true
}