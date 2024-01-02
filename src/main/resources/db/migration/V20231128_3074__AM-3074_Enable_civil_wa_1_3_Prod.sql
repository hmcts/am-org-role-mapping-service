-- enable civil_wa_1_3 flag in Prod for: AM-3074
update flag_config set status='true' where flag_name='civil_wa_1_3' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');