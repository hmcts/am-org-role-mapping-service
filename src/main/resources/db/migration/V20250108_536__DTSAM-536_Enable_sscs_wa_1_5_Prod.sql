-- enable sscs_wa_1_5 flag in Prod for: DTSAM-536 / DTSAM-506
update flag_config set status='true' where flag_name='sscs_wa_1_5' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
