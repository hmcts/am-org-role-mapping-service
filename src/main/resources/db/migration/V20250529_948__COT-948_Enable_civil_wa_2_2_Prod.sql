-- enable civil_wa_2_2 flag in Prod for: COT-948 / COT-945
update flag_config set status='true' where flag_name='civil_wa_2_2' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
