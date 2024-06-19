-- DTSAM-344 Spring Boot 3 Upgrade RARB - spring-batch tables appended with v4 for rollback
alter table batch_job_execution_context rename to batch_job_execution_context_v4;
alter table batch_step_execution_context rename to batch_step_execution_context_v4;
alter table batch_step_execution rename to batch_step_execution_v4;
alter table batch_job_execution_params rename to batch_job_execution_params_v4;
alter table batch_job_execution rename to batch_job_execution_v4;
alter table batch_job_instance rename to batch_job_instance_v4;