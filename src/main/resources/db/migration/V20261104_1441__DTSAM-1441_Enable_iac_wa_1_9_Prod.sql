-- enable iac_wa_1_9 flag in Prod for: DTSAM-1441 / DTSAM-1438
update flag_config set status='true' where flag_name='iac_wa_1_9' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
