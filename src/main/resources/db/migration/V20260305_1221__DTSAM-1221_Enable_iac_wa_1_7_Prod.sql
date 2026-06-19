-- enable iac_wa_1_7 flag in Prod for: DTSAM-1221 / DTSAM-1217
update flag_config set status='true' where flag_name='iac_wa_1_7' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
