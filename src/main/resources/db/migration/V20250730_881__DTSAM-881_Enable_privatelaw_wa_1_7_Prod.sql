-- enable privatelaw_wa_1_7 flag in Prod for: DTSAM-881 / DTSAM-860
update flag_config set status='true' where flag_name='privatelaw_wa_1_7' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
