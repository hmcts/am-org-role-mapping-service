-- enable publiclaw_wa_1_3 flag in Prod for: DTSAM-143
update flag_config set status='true' where flag_name='publiclaw_wa_1_3' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');