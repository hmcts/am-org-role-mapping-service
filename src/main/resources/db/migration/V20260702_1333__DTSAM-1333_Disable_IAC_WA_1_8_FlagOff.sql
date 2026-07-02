-- TEMP disable iac_wa_1_8 flag in PREVIEW for testing of DTSAM-1333
update flag_config set status='false' where flag_name='iac_wa_1_8' and env in ('pr');