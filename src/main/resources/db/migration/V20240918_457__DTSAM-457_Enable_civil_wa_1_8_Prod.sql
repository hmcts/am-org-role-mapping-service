-- enable civil_wa_1_8 flag in Prod for: DTSAM-457 / DTSAM-443
update flag_config set status='true' where flag_name='civil_wa_1_8' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');