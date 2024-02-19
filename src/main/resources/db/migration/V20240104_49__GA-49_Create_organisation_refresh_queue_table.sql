CREATE TABLE organisation_refresh_queue (
    organisation_id text PRIMARY KEY,
    last_updated timestamp without time zone NOT NULL,
    access_types_min_version integer NOT NULL,
    active bool NOT NULL,
    retry integer default 0
);