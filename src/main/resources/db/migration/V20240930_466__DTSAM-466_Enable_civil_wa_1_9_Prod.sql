-- enable civil_wa_1_9 flag in Prod for: DTSAM-466 / DTSAM-452
update flag_config set status='true' where flag_name='civil_wa_1_9' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
