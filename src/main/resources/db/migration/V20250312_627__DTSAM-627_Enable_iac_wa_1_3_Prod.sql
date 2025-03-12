-- enable iac_wa_1_3 flag in Prod for: DTSAM-627
update flag_config set status='true' where flag_name='iac_wa_1_3' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
