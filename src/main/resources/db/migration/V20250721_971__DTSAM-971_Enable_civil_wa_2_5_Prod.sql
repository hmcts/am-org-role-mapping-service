-- enable civil_wa_2_5 flag in Prod for: DTSAM-971 / DTSAM-956
update flag_config set status='true' where flag_name='civil_wa_2_5' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
