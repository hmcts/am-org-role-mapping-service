-- TEMP disable fr_wa_1_0 flag in PREVIEW for testing of COT-1208
update flag_config set status='false' where flag_name='fr_wa_1_0' and env in ('pr');
