-- enable PUBLICLAW flag for: COT-358
update flag_config set status='true' where flag_name='publiclaw_wa_1_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');