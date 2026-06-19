-- COT-1031: insert iac_wa_1_4 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'local', 'iac', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'pr', 'iac', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'aat', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'demo', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'perftest', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'ithc', 'iac', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'prod', 'iac', 'false');