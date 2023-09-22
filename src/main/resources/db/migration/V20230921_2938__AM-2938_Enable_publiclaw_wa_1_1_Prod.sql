-- enable publiclaw_wa_1_1 flag in Prod for: AM-2938
update flag_config set status='true' where flag_name='publiclaw_wa_1_1' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');