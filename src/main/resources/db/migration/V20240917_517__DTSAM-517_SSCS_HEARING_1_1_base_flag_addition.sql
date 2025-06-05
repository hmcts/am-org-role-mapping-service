-- DTSAM-517: insert sscs_hearing_1_1 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_hearing_1_1', 'local', 'sscs', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_hearing_1_1', 'pr', 'sscs', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_hearing_1_1', 'aat', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_hearing_1_1', 'demo', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_hearing_1_1', 'perftest', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_hearing_1_1', 'ithc', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_hearing_1_1', 'prod', 'sscs', 'false');