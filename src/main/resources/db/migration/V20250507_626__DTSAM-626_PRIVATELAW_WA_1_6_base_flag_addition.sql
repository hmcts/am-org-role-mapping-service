-- DTSAM-668: insert privatelaw_wa_1_6 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_6', 'local', 'privatelaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_6', 'pr', 'privatelaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_6', 'aat', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_6', 'demo', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_6', 'perftest', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_6', 'ithc', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_6', 'prod', 'privatelaw', 'false');