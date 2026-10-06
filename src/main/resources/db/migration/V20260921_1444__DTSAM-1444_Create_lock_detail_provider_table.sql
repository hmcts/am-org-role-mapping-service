CREATE TABLE lock_details_provider (
      "name" varchar(64) NOT NULL,
      lock_until timestamp NOT NULL,
      locked_at timestamp NOT NULL,
      locked_by varchar(255) NOT NULL,
      CONSTRAINT lock_details_provider_pkey PRIMARY KEY (name)
);