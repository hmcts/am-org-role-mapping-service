-- enable iac_wa_1_6 flag in Prod for: DTSAM-1184 / DTSAM-1180
update flag_config set status='true' where flag_name='iac_wa_1_6' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
