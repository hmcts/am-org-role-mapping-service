-- create table
CREATE TABLE profile_refresh_queue(
	organisation_profile_id text not null,
    access_types_min_version integer NOT NULL,
	active bool NOT NULL,
	CONSTRAINT profile_refresh_queue_pkey PRIMARY KEY (organisation_profile_id)
);
