-- create table
-- PK/FK relationships in table access_types
-- access_types pk column = version
-- automatic when creating bigserial:
--   access_types_version_seq (Sequence)
--   nextval('access_types_version_seq') NOT null

CREATE TABLE access_types(
	version bigserial not null,
    access_types jsonb NOT NULL,
	CONSTRAINT pk_access_types PRIMARY KEY (version)
);


