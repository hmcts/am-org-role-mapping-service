-- DTSAM-344 Spring Boot 3 Upgrade RARB - spring-batch tables backed up and appended with v4 for rollback

DO $$
    BEGIN
        IF EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_execution_context'
            )
        THEN
            create table batch_job_execution_context_v4 (like batch_job_execution_context including all);
            insert into batch_job_execution_context_v4 select * from batch_job_execution_context;
        END IF ;

        IF EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_step_execution_context'
            )
        THEN
            create table batch_step_execution_context_v4 (like batch_step_execution_context including all);
            insert into batch_step_execution_context_v4 select * from batch_step_execution_context;
        END IF ;

        IF EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_step_execution'
            )
        THEN
            create table batch_step_execution_v4 (like batch_step_execution including all);
            insert into batch_step_execution_v4 select * from batch_step_execution;
        END IF ;

        IF EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_execution_params'
            )
        THEN
            create table batch_job_execution_params_v4 (like batch_job_execution_params including all);
            insert into batch_job_execution_params_v4 select * from batch_job_execution_params;
        END IF ;

        IF EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_execution'
            )
        THEN
            create table batch_job_execution_v4 (like batch_job_execution including all);
            insert into batch_job_execution_v4 select * from batch_job_execution;
        END IF ;

        IF EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'batch_job_instance'
            )
        THEN
            create table batch_job_instance_v4 (like batch_job_instance including all);
            insert into batch_job_instance_v4 select * from batch_job_instance;
        END IF ;


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
                    drop sequence if exists BATCH_STEP_EXECUTION_SEQ;
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
                    drop sequence if exists BATCH_JOB_EXECUTION_SEQ;
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
                    drop sequence if exists BATCH_JOB_SEQ;

                END IF ;
    END
$$ ;