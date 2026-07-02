-- COT-1208: ensure FR WA is enabled for PR and AAT functional tests.
INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'local', 'divorce', 'true'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'local');

INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'pr', 'divorce', 'true'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'pr');

INSERT INTO flag_config (flag_name, env, service_name, status)
SELECT 'fr_wa_1_0', 'aat', 'divorce', 'true'
WHERE NOT EXISTS (SELECT 1 FROM flag_config WHERE flag_name = 'fr_wa_1_0' AND env = 'aat');

UPDATE flag_config SET service_name = 'divorce', status = 'true'
WHERE flag_name = 'fr_wa_1_0' AND env IN ('local', 'pr', 'aat');
