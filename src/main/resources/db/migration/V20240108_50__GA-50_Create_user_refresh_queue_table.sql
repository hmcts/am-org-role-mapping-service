CREATE TABLE user_refresh_queue (
    user_id text PRIMARY KEY,
    user_last_updated timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL default now(),
    access_types_min_version int NOT NULL,
    deleted timestamp without time zone,
    access_types jsonb NOT NULL default '[]'::jsonb,
    organisation_id text NOT NULL,
    organisation_status text NOT NULL,
    organisation_profile_ids text[] NOT NULL,
    active boolean NOT NULL,
    retry integer default 0,
    retry_after timestamp without time zone default now()
);