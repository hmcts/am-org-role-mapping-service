-- COT-1149: insert probate_wa_1_0 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('probate_wa_1_0', 'local', 'probate', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('probate_wa_1_0', 'pr', 'probate', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('probate_wa_1_0', 'aat', 'probate', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('probate_wa_1_0', 'demo', 'probate', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('probate_wa_1_0', 'perftest', 'probate', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('probate_wa_1_0', 'ithc', 'probate', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('probate_wa_1_0', 'prod', 'probate', 'false');