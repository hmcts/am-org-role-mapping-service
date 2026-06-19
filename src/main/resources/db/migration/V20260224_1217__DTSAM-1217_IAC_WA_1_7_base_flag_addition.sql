-- DTSAM-1217: insert iac_wa_1_7 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_7', 'local', 'iac', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_7', 'pr', 'iac', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_7', 'aat', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_7', 'demo', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_7', 'perftest', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_7', 'ithc', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_7', 'prod', 'iac', 'false');