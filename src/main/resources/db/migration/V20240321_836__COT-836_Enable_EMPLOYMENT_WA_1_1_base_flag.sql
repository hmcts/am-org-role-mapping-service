-- enable employment_wa_1_1 flag in Prod for: COT-836
update flag_config set status='true' where flag_name='employment_wa_1_1' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
