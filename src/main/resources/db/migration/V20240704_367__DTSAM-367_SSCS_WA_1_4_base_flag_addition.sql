-- DTSAM-367: insert sscs_wa_1_4 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_4', 'local', 'sscs', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_4', 'pr', 'sscs', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_4', 'aat', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_4', 'demo', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_4', 'perftest', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_4', 'ithc', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_4', 'prod', 'sscs', 'false');