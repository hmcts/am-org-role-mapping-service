-- DTSAM-442 Grant read access to refresh job sequence to the prod/non-prod reader accounts

DO $$
    BEGIN
        IF EXISTS
            ( SELECT 1
              FROM   pg_roles
              WHERE  rolname = 'DTS CFT DB Access Reader'
            )
        THEN
            grant select on sequence job_id_seq to "DTS CFT DB Access Reader";
        END IF ;

        IF EXISTS
            ( SELECT 1
              FROM   pg_roles
              WHERE  rolname = 'DTS JIT Access am DB Reader SC'
            )
        THEN
            grant select on sequence job_id_seq to "DTS JIT Access am DB Reader SC";
        END IF ;
    END
$$ ;