CREATE TABLE user_refresh_queue (
    user_id text PRIMARY KEY,
    last_updated timestamp without time zone NOT NULL,
    access_types_min_version int NOT NULL,
    deleted timestamp without time zone,
    access_types jsonb NOT NULL default '[]'::jsonb,
    organisation_id text NOT NULL,
    organisation_status text NOT NULL,
    organisation_profile_ids text[] NOT NULL,
    active boolean NOT NULL
);