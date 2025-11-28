-- enable privatelaw_wa_1_5 flag in Prod for: DTSAM-672
update flag_config set status='true' where flag_name='privatelaw_wa_1_5' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
