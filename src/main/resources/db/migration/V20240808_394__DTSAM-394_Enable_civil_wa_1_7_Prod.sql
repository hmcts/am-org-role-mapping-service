-- enable civil_wa_1_7 flag in Prod for: DTSAM-394 / DTSAM-384
update flag_config set status='true' where flag_name='civil_wa_1_7' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');