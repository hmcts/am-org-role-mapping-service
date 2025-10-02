-- DTSAM-860: insert privatelaw_wa_1_7 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_7', 'local', 'privatelaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_7', 'pr', 'privatelaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_7', 'aat', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_7', 'demo', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_7', 'perftest', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_7', 'ithc', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_1_7', 'prod', 'privatelaw', 'false');