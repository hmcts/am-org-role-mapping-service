-- DTSAM-826: insert publiclaw_wa_2_0 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_2_0', 'local', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_2_0', 'pr', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_2_0', 'aat', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_2_0', 'demo', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_2_0', 'perftest', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_2_0', 'ithc', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_wa_2_0', 'prod', 'publiclaw', 'false');