-- enable privatelaw_hearing_1_0 flag in Prod for: DTSAM-545 / DTSAM-517
update flag_config set status='true' where flag_name='privatelaw_hearing_1_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
