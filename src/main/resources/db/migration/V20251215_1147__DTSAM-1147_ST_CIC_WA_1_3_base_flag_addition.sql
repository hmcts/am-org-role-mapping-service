---- DTSAM-1147: insert st_cic_wa_1_3 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('st_cic_wa_1_3', 'local', 'st_cic', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('st_cic_wa_1_3', 'pr', 'st_cic', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('st_cic_wa_1_3', 'aat', 'st_cic', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('st_cic_wa_1_3', 'demo', 'st_cic', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('st_cic_wa_1_3', 'perftest', 'st_cic', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('st_cic_wa_1_3', 'ithc', 'st_cic', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('st_cic_wa_1_3', 'prod', 'st_cic', 'false');
