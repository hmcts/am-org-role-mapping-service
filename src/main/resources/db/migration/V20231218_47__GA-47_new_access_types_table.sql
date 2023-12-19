-- create table
-- PK/FK relationships in tables access_types
-- access_types pk column = version

CREATE TABLE access_types(
	version bigint not null,
    access_type jsonb NOT NULL,
	CONSTRAINT pk_access_types PRIMARY KEY (version)
);


-- create sequence
create sequence VERSION_SEQ;
-- add sequence to table

ALTER TABLE access_types ALTER COLUMN version
SET DEFAULT nextval('VERSION_SEQ'::regclass);

