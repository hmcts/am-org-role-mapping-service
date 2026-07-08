-- enable privatelaw_wa_1_9 flag in Prod for: DTSAM-1313 / DTSAM-1310
update flag_config set status='true' where flag_name='privatelaw_wa_1_9' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
