-- enable hrs_1_0 flag in Prod for: DTSAM-1248 / DTSAM-1214
update flag_config set status='true' where flag_name='hrs_1_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
