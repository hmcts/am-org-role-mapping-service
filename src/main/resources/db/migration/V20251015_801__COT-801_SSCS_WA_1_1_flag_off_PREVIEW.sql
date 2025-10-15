-- TEMP disable sscs_wa_1_1 flag in PREVIEW for testing of COT-801/DTSAM-234
update flag_config set status='false' where flag_name='sscs_wa_1_1' and env in ('pr');
