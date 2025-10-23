-- COT-1031: insert iac_wa_1_4 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'local', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'pr', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'aat', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'demo', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'perftest', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'ithc', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('iac_wa_1_4', 'prod', 'civil', 'false');