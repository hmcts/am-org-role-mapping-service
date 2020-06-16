DELETE FROM role_assignment_request;

INSERT INTO public.role_assignment_request
(id, correlation_id, client_id, authenticated_user_id, requester_id, request_type, status, process, reference, replace_existing, last_updated, created)
VALUES('11334a2b-79ce-44eb-9168-2d49a744be9c', 'correlation_id', 'client_id', '41334a2b-79ce-44eb-9168-2d49a744beaa', '41334a2b-79ce-44eb-9168-2d49a744bebb',
'request_type', 'created', 'process', 'reference', false, now(), now());

INSERT INTO public.role_assignment_request
(id, correlation_id, client_id, authenticated_user_id, requester_id, request_type, status, process, reference, replace_existing, last_updated, created)
VALUES('21334a2b-79ce-44eb-9168-2d49a744be9c', 'correlation_id', 'client_id', '41334a2b-79ce-44eb-9168-2d49a744beaa', '41334a2b-79ce-44eb-9168-2d49a744bebb',
'request_type', 'APPROVED', 'process', 'reference', false, now(), now());

INSERT INTO public.role_assignment_request
(id, correlation_id, client_id, authenticated_user_id, requester_id, request_type, status, process, reference, replace_existing, last_updated, created)
VALUES('31334a2b-79ce-44eb-9168-2d49a744be9c', 'correlation_id', 'client_id', '41334a2b-79ce-44eb-9168-2d49a744beaa', '41334a2b-79ce-44eb-9168-2d49a744bebb',
'request_type', 'created', 'process', 'reference', false, now(), now());

INSERT INTO public.role_assignment_request
(id, correlation_id, client_id, authenticated_user_id, requester_id, request_type, status, process, reference, replace_existing, last_updated, created)
VALUES('51334a2b-79ce-44eb-9168-2d49a744be9c', 'correlation_id', 'client_id', '41334a2b-79ce-44eb-9168-2d49a744beaa', '41334a2b-79ce-44eb-9168-2d49a744bebb',
'request_type', 'created', 'process', 'reference', false, now(), now());

INSERT INTO public.role_assignment_request
(id, correlation_id, client_id, authenticated_user_id, requester_id, request_type, status, process, reference, replace_existing, last_updated, created)
VALUES('61334a2b-79ce-44eb-9168-2d49a744be9c', 'correlation_id', 'client_id', '41334a2b-79ce-44eb-9168-2d49a744beaa', '41334a2b-79ce-44eb-9168-2d49a744bebb',
'request_type', 'created', 'process', 'reference', false, now(), now());
