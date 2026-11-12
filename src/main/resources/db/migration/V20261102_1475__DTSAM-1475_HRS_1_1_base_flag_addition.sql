-- DTSAM-1475: insert hrs_1_1 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('hrs_1_1', 'local', 'hrs', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('hrs_1_1', 'pr', 'hrs', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('hrs_1_1', 'aat', 'hrs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('hrs_1_1', 'demo', 'hrs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('hrs_1_1', 'perftest', 'hrs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('hrs_1_1', 'ithc', 'hrs', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('hrs_1_1', 'prod', 'hrs', 'false');