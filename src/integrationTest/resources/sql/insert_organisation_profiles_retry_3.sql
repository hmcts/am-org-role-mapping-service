DELETE FROM organisation_refresh_queue;

INSERT INTO organisation_refresh_queue (organisation_id, last_updated, access_types_min_version, active, retry)
VALUES ('123', '2024-02-06 12:51:11.158', 1, true, 3);