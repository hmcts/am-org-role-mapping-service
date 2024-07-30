-- DTSAM-434: insert publiclaw_wa_1_4 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_4', 'local', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_4', 'pr', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_4', 'aat', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_4', 'demo', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_4', 'perftest', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_4', 'ithc', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_4', 'prod', 'publiclaw', 'false');