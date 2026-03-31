-- TEMP disable probate_wa_1_0 flag in PREVIEW for testing of COT-1149
update flag_config set status='false' where flag_name='probate_wa_1_0' and env in ('pr');
