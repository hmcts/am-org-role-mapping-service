-- DTSAM-801: insert civil_wa_2_4 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_4', 'local', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_4', 'pr', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_4', 'aat', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_4', 'demo', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_4', 'perftest', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_4', 'ithc', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_4', 'prod', 'civil', 'false');
