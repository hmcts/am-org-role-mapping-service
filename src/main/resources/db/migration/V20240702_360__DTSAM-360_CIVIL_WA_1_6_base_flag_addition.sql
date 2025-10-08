-- DTSAM-360: insert civil_wa_1_6 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_6', 'local', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_6', 'pr', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_6', 'aat', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_6', 'demo', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_6', 'perftest', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_6', 'ithc', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_6', 'prod', 'civil', 'false');