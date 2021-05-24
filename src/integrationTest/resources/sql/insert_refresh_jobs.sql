DELETE FROM refresh_jobs;

INSERT INTO public.refresh_jobs (job_id, role_category, jurisdiction, status, user_ids, linked_job_id, created)
VALUES(1, 'LEGAL_OPERATIONS', 'IAC', 'NEW', NULL, 0, NULL);
INSERT INTO public.refresh_jobs (job_id, role_category, jurisdiction, status, user_ids, linked_job_id, created)
VALUES(2, 'LEGAL_OPERATIONS', 'IAC', 'ABORTED', '{"7c12a4bc-450e-4290-8063-b387a5d5e0b7"}', NULL, NULL);
INSERT INTO public.refresh_jobs (job_id, role_category, jurisdiction, status, user_ids, linked_job_id, created)
VALUES(3, 'LEGAL_OPERATIONS', 'IAC', 'NEW', NULL, 2, NULL);
INSERT INTO public.refresh_jobs (job_id, role_category, jurisdiction, status, user_ids, linked_job_id, created)
VALUES(4, 'LEGAL_OPERATIONS', 'IAC', 'COMPLETED', NULL, 2, NULL);


