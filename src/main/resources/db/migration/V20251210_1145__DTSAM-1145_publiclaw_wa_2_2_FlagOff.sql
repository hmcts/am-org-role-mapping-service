-- TEMP disable publiclaw_wa_2_2 flag in PREVIEW for testing of DTSAM-1145/DTSAM-1133
update flag_config set status='false' where flag_name='publiclaw_wa_2_2' and env in ('pr');