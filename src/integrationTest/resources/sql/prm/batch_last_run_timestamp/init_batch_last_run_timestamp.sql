DELETE FROM batch_last_run_timestamp;

insert into batch_last_run_timestamp (id, last_user_run_date_time, last_organisation_run_date_time)
values (1, '2000-01-01 00:00:00', '2000-01-01 00:00:00');
