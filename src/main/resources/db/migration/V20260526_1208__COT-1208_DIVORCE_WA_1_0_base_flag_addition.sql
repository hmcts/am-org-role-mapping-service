-- COT-1208: insert divorce_wa_1_0 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('divorce_wa_1_0', 'local', 'divorce', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('divorce_wa_1_0', 'pr', 'divorce', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('divorce_wa_1_0', 'aat', 'divorce', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('divorce_wa_1_0', 'demo', 'divorce', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('divorce_wa_1_0', 'perftest', 'divorce', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('divorce_wa_1_0', 'ithc', 'divorce', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('divorce_wa_1_0', 'prod', 'divorce', 'false');
