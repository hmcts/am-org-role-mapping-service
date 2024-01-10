CREATE TABLE batch_last_run_timestamp (
    batch_last_run_timestamp_id bigserial PRIMARY KEY NOT NULL,
    last_user_run_date_time timestamp not null default NOW(),
    last_organisation_run_date_time timestamp not null default NOW()
);