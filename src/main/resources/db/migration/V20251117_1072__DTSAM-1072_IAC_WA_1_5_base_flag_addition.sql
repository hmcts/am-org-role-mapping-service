-- DTSAM-1072: insert iac_wa_1_5 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_5', 'local', 'iac', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_5', 'pr', 'iac', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_5', 'aat', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_5', 'demo', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_5', 'perftest', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_5', 'ithc', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_5', 'prod', 'iac', 'false');