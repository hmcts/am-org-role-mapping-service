-- enable civil_wa_1_2 flag in Prod for: AM-3072
update flag_config set status='true' where flag_name='civil_wa_1_2' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');