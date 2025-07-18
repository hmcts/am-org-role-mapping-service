-- DTSAM-384: insert civil_wa_1_7 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_7', 'local', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_7', 'pr', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_7', 'aat', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_7', 'demo', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_7', 'perftest', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_7', 'ithc', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_1_7', 'prod', 'civil', 'false');