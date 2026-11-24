-- enable fr_wa_1_0 flag in Prod for: COT-1216
update flag_config set status='true' where flag_name='fr_wa_1_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
