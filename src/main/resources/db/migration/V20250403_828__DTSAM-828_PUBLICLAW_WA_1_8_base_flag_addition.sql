-- DTSAM-828: insert publiclaw_wa_1_8 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_8', 'local', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_8', 'pr', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_8', 'aat', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_8', 'demo', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_8', 'perftest', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_8', 'ithc', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_8', 'prod', 'publiclaw', 'false');
