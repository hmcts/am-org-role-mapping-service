-- irm-101: insert 'irm_wa_1_0' base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('irm_wa_1_0', 'local', 'irm', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('irm_wa_1_0', 'pr', 'irm', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('irm_wa_1_0', 'aat', 'irm', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('irm_wa_1_0', 'demo', 'irm', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('irm_wa_1_0', 'perftest', 'irm', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('irm_wa_1_0', 'ithc', 'irm', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('irm_wa_1_0', 'prod', 'irm', 'false');
