-- DTSAM-409: insert employment_wa_1_4 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_4', 'local', 'employment', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_4', 'pr', 'employment', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_4', 'aat', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_4', 'demo', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_4', 'perftest', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_4', 'ithc', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_4', 'prod', 'employment', 'false');