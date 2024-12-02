-- enable civil_wa_2_0 flag in Prod for: DTSAM-595 / DTSAM-575
update flag_config set status='true' where flag_name='civil_wa_2_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
