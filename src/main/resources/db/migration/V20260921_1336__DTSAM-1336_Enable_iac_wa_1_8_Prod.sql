-- enable iac_wa_1_8 flag in Prod for: DTSAM-1336 / DTSAM-1333
update flag_config set status='true' where flag_name='iac_wa_1_8' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
