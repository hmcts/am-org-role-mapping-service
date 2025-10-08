-- enable employment_wa_1_0 flag in Prod for: AM-3077
update flag_config set status='true' where flag_name='employment_wa_1_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');