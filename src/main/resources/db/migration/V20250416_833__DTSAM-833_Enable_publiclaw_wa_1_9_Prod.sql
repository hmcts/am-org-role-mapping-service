-- enable publiclaw_wa_1_9 flag in Prod for: DTSAM-833 / DTSAM-840
update flag_config set status='true' where flag_name='publiclaw_wa_1_9' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
