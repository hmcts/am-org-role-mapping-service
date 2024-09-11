-- enable civil_wa_1_6 flag in Prod for: DTSAM-392 / DTSAM-360
update flag_config set status='true' where flag_name='civil_wa_1_6' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');