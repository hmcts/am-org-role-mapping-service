-- DTSAM-1460: insert privatelaw_wa_2_0 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_2_0', 'local', 'privatelaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_2_0', 'pr', 'privatelaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_2_0', 'aat', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_2_0', 'demo', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_2_0', 'perftest', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_2_0', 'ithc', 'privatelaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('privatelaw_wa_2_0', 'prod', 'privatelaw', 'false');