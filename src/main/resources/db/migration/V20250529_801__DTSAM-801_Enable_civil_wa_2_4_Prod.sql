-- enable civil_wa_2_4 flag in Prod for: DTSAM-801
update flag_config set status='true' where flag_name='civil_wa_2_4' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
