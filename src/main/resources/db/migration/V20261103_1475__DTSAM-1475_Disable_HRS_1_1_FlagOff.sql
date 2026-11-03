-- TEMP disable hrs_1_1 flag in PREVIEW for testing of DTSAM-1475
update flag_config set status='false' where flag_name='hrs_1_1' and env in ('pr');
