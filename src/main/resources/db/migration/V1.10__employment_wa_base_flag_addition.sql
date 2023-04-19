-- insert employment law base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_0', 'local', 'employment', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_0', 'pr', 'employment', 'true');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_0', 'aat', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_0', 'demo', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_0', 'perftest', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_0', 'ithc', 'employment', 'false');
INSERT INTO flag_config (flag_name, env, service_name, status) VALUES ('employment_wa_1_0', 'prod', 'employment', 'false');