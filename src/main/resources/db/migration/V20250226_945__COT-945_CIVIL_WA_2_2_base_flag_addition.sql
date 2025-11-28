-- COT-945: insert civil_wa_2_2 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_2', 'local', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_2', 'pr', 'civil', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_2', 'aat', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_2', 'demo', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_2', 'perftest', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_2', 'ithc', 'civil', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('civil_wa_2_2', 'prod', 'civil', 'false');