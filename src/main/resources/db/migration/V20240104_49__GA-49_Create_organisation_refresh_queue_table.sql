CREATE TABLE organisation_refresh_queue (
    organisation_id text PRIMARY KEY,
    organisation_last_updated timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL default now(),
    access_types_min_version integer NOT NULL,
    active bool NOT NULL,
    retry integer default 0,
    retry_after timestamp without time zone default now()
);