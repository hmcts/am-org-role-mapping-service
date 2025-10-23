-- TEMP disable iac_wa_1_4 flag in PREVIEW for testing of COT-1031
update flag_config set status='false' where flag_name='iac_wa_1_4' and env in ('pr');
