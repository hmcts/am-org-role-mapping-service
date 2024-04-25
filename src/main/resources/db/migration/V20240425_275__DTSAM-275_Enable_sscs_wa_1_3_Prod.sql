-- enable sscs_wa_1_3 flag in Prod for: DTSAM-275
update flag_config set status='true' where flag_name='sscs_wa_1_3' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');