-- PK/FK relationships in table batch_last_run_timestamp
-- batch_last_run_timestamp pk column = id
-- automatically creates when creating bigserial:
--   batch_last_run_timestamp_id_seq (Sequence)
--   nextval('batch_last_run_timestamp_id_seq') NOT null
-- timestamp - without time zone
CREATE TABLE batch_last_run_timestamp (
    id bigserial PRIMARY KEY,
    last_user_run_date_time timestamp not null default NOW(),
    last_organisation_run_date_time timestamp not null default NOW()
);