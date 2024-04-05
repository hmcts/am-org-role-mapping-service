-- enable publiclaw_wa_1_4 flag in Prod for: DTSAM-248
update flag_config set status='true' where flag_name='publiclaw_wa_1_4' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');