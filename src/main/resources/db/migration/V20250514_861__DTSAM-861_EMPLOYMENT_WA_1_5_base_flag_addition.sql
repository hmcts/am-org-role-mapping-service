-- DTSAM-861: insert employment_wa_1_5 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_5', 'local', 'employment', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_5', 'pr', 'employment', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_5', 'aat', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_5', 'demo', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_5', 'perftest', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_5', 'ithc', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_5', 'prod', 'employment', 'false');