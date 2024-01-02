-- enable privatelaw_wa_1_3 flag in Prod for: AM-3018 / AM-3032
update flag_config set status='true' where flag_name='privatelaw_wa_1_3' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');