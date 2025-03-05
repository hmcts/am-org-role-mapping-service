-- enable iac_jrd_1_1 flag in Prod for: DTSAM-149 / DTSAM-96
update flag_config set status='true' where flag_name='iac_jrd_1_1' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
