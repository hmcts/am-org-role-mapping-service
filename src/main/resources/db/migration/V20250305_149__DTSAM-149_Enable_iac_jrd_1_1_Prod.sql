-- enable civil_wa_2_1 flag in Prod for: COT-918 / COT-906
update flag_config set status='true' where flag_name='iac_jrd_1_1' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
