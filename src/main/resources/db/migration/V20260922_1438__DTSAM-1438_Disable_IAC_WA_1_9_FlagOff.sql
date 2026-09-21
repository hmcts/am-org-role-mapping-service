-- TEMP disable iac_wa_1_9 flag in PREVIEW for testing of DTSAM-1438
update flag_config set status='false' where flag_name='iac_wa_1_9' and env in ('pr');