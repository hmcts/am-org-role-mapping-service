CREATE TABLE actor_cache_control(
	actor_id text NOT NULL,
	etag int4 NOT NULL,
	json_response jsonb NOT NULL,
	CONSTRAINT actor_cache_control_pkey PRIMARY KEY (actor_id)
);