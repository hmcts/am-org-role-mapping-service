-- enable sscs_wa_1_2 flag in Prod for: DTSAM-266
update flag_config set status='true' where flag_name='sscs_wa_1_2' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');