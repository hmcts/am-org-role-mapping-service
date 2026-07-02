-- COT-1208: insert fr_wa_1_0 base flag into flag_config table
INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'local', 'divorce', 'true'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'local');

INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'pr', 'divorce', 'true'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'pr');

INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'preview', 'divorce', 'true'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'preview');

INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'aat', 'divorce', 'true'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'aat');

INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'demo', 'divorce', 'false'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'demo');

INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'perftest', 'divorce', 'false'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'perftest');

INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'ithc', 'divorce', 'false'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'ithc');

INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'prod', 'divorce', 'false'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'prod');

UPDATE flag_config SET service_name = 'divorce', status = 'true'
WHERE flag_name = 'fr_wa_1_0' AND env IN ('local', 'pr', 'preview', 'aat');

UPDATE flag_config SET service_name = 'divorce', status = 'false'
WHERE flag_name = 'fr_wa_1_0' AND env IN ('demo', 'perftest', 'ithc', 'prod');
