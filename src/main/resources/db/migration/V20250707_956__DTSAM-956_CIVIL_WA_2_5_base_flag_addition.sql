-- DTSAM-956: insert civil_wa_2_5 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_5', 'local', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_5', 'pr', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_5', 'aat', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_5', 'demo', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_5', 'perftest', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_5', 'ithc', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_5', 'prod', 'civil', 'false');
