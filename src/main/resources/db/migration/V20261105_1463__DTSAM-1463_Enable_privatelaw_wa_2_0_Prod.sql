-- enable privatelaw_wa_2_0 flag in Prod for: DTSAM-1463 / DTSAM-1460
update flag_config set status='true' where flag_name='privatelaw_wa_2_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
