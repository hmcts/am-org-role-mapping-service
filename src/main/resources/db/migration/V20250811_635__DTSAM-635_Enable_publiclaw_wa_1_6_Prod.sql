-- enable publiclaw_wa_1_6 flag in Prod for: DTSAM-635 / DTSAM-617 & DTSAM-625
update flag_config set status='true' where flag_name='publiclaw_wa_1_6' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
