-- DTSAM-464: insert publiclaw_wa_1_5 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_5', 'local', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_5', 'pr', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_5', 'aat', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_5', 'demo', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_5', 'perftest', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_5', 'ithc', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_1_5', 'prod', 'publiclaw', 'false');