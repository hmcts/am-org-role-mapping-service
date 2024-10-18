-- DTSAM-344 Spring Boot 3 Upgrade RARB - spring-batch tables dropped if backup _v4 exists

DO $$
    BEGIN
        IF (EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_execution_context'
            )
            AND EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_execution_context_v4'
            ))
        THEN
            drop table batch_job_execution_context;
        END IF ;

       IF (EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_step_execution_context'
            )
            AND EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_step_execution_context_v4'
            ))
        THEN
            drop table batch_step_execution_context;
        END IF ;

       IF (EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_step_execution'
            )
            AND EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_step_execution_v4'
            ))
        THEN
            drop table batch_step_execution;
        END IF ;

       IF (EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_execution_params'
            )
            AND EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_execution_params_v4'
            ))
        THEN
            drop table batch_job_execution_params;
        END IF ;

       IF (EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_execution'
            )
            AND EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_execution_v4'
            ))
        THEN
            drop table batch_job_execution;
        END IF ;

       IF (EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_instance'
            )
            AND EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_instance_v4'
            ))
        THEN
            drop table batch_job_instance;
        END IF ;
    END
$$ ;