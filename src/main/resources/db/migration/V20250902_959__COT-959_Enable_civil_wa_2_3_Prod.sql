-- enable civil_wa_2_3 flag in Prod for: COT-959 / COT-958 & COT-1003
update flag_config set status='true' where flag_name='civil_wa_2_3' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
