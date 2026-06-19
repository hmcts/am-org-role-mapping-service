-- enable publiclaw_wa_2_2 flag in Prod for: DTSAM-1136 / DTSAM-1132 & DTSAM-1145
update flag_config set status='true' where flag_name='publiclaw_wa_2_2' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
