-- DTSAM-506: insert sscs_wa_1_5 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_5', 'local', 'sscs', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_5', 'pr', 'sscs', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_5', 'aat', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_5', 'demo', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_5', 'perftest', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_5', 'ithc', 'sscs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('sscs_wa_1_5', 'prod', 'sscs', 'false');