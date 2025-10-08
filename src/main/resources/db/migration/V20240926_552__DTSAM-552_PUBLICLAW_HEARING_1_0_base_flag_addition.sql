-- DTSAM-552: insert publiclaw_hearing_1_0 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_hearing_1_0', 'local', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_hearing_1_0', 'pr', 'publiclaw', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_hearing_1_0', 'aat', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_hearing_1_0', 'demo', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_hearing_1_0', 'perftest', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_hearing_1_0', 'ithc', 'publiclaw', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('publiclaw_hearing_1_0', 'prod', 'publiclaw', 'false');