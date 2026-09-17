-- POFCC-101: insert 'possessions_wa_1_0' base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('possessions_wa_1_1', 'local', 'pofcc', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('possessions_wa_1_1', 'pr', 'pofcc', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('possessions_wa_1_1', 'aat', 'pofcc', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('possessions_wa_1_1', 'demo', 'pofcc', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('possessions_wa_1_1', 'perftest', 'pofcc', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('possessions_wa_1_1', 'ithc', 'pofcc', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('possessions_wa_1_1', 'prod', 'pofcc', 'false');
