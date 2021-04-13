create table flag_state(
	flag_id text not null,
	state boolean,
	constraint flag_state_pkey PRIMARY KEY (flag_id)
);

create table refresh_jobs(
	job_id bigint not null,
	user_type text not null,
	jurisdiction text not null,
	status text not null,
	locked_by text,
	unlock_at timestamp,
	retries_count int not null,
	log text,
	created timestamp,
	constraint refresh_jobs_pkey PRIMARY KEY (job_id)
);

create sequence JOB_ID_SEQ;
ALTER TABLE refresh_jobs ALTER COLUMN job_id
SET DEFAULT nextval('JOB_ID_SEQ');