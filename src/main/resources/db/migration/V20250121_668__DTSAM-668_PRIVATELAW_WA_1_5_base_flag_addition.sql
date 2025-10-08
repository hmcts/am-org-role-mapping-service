-- DTSAM-668: insert privatelaw_wa_1_5 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_5', 'local', 'privatelaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_5', 'pr', 'privatelaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_5', 'aat', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_5', 'demo', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_5', 'perftest', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_5', 'ithc', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_5', 'prod', 'privatelaw', 'false');