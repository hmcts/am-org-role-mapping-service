DELETE FROM batch_last_run_timestamp;

INSERT INTO batch_last_run_timestamp (id, last_user_run_date_time, last_organisation_run_date_time)
VALUES (1, '2024-02-01 12:34:56.789',  '2024-02-02 12:34:56.789');
