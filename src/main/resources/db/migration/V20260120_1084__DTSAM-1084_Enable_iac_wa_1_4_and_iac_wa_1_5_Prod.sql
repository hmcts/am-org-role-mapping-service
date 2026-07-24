-- enable iac_wa_1_4 flag in Prod for: COT-1034 / COT-1031
update flag_config set status='true' where flag_name='iac_wa_1_4' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');

-- enable iac_wa_1_5 flag in Prod for: DTSAM-1084 / DTSAM-1072
update flag_config set status='true' where flag_name='iac_wa_1_5' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
