-- enable privatelaw_wa_1_8 flag in Prod for: DTSAM-961 / DTSAM-932 & DTSAM-960
update flag_config set status='true' where flag_name='privatelaw_wa_1_8' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
