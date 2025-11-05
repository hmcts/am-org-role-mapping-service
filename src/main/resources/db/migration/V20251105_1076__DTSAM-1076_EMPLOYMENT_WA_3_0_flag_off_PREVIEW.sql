-- TEMP disable employment_wa_3_0 flag in PREVIEW for testing of DTSAM-1076
update flag_config set status='false' where flag_name='employment_wa_3_0' and env in ('pr');
