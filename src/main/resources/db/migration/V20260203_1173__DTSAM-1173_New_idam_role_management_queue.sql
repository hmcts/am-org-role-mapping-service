-- create table idam_role_management_queue
CREATE TABLE idam_role_management_queue(
   user_id text PRIMARY KEY,
   user_type text NOT NULL,
   last_updated timestamp without time zone NOT NULL default now(),
   last_published timestamp without time zone,
   data jsonb NOT NULL default '[]'::jsonb,
   published_as text NOT NULL,
   active boolean NOT NULL,
   retry integer default 0,
   retry_after timestamp without time zone default now()
);