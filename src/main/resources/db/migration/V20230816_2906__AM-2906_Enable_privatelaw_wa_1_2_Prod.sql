-- enable PUBLICLAW flag for: AM-2906
update flag_config set status='true' where flag_name='publiclaw_wa_1_2' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');