CREATE TABLE batch_last_run_timestamp (
    id bigserial PRIMARY KEY,
    last_user_run_date_time timestamp without time zone not null,
    last_organisation_run_date_time timestamp without time zone not null
);

-- default to a date earlier than any last_updated value for users or organisations in PRD
-- ensures that on the first run of process 3 & 5, all PRD users/organisations will be retrieved and processed
insert into batch_last_run_timestamp (id, last_user_run_date_time, last_organisation_run_date_time)
values (1, '2000-01-01 00:00:00', '2000-01-01 00:00:00');