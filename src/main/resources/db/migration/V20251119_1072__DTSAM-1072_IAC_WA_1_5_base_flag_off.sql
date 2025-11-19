-- TEMP disable iac_wa_1_5 flag in PREVIEW for testing of DTSAM-1072/DTSAM-1081
update flag_config set status='false' where flag_name='iac_wa_1_5' and env in ('pr');