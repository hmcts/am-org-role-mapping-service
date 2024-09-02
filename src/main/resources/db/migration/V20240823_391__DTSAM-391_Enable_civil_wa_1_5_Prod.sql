-- enable civil_wa_1_5 flag in Prod for: DTSAM-391 / DTSAM-353
update flag_config set status='true' where flag_name='civil_wa_1_5' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');