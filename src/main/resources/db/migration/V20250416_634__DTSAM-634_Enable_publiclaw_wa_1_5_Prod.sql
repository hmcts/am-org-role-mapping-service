-- enable publiclaw_wa_1_5 flag in Prod for: DTSAM-634 / DTSAM-464 & DTSAM-616
update flag_config set status='true' where flag_name='publiclaw_wa_1_5' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
