-- DTSAM-590: insert iac_wa_1_3 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_3', 'local', 'iac', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_3', 'pr', 'iac', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_3', 'aat', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_3', 'demo', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_3', 'perftest', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_3', 'ithc', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_3', 'prod', 'iac', 'false');