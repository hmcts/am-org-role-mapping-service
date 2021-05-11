
create table refresh_jobs(
	job_id bigint not null,
	role_category text not null,
	jurisdiction text not null,
	status text not null,
	comments text,
	user_ids _text NULL,
	log text,
	linked_job_id bigint,
	created timestamp,
	constraint refresh_jobs_pkey PRIMARY KEY (job_id)
);

create sequence JOB_ID_SEQ;
ALTER TABLE refresh_jobs ALTER COLUMN job_id
SET DEFAULT nextval('JOB_ID_SEQ');

INSERT INTO public.refresh_jobs
(role_category, jurisdiction, status, "comments", user_ids, log, linked_job_id, created)
VALUES('LEGAL_OPERATIONS', 'IAC', 'NEW', '', '{}', '', '0', '2021-05-10 14:53:48');


