-- enable publiclaw_wa_2_1 flag in Prod for: DTSAM-965 / DTSAM-939
update flag_config set status='true' where flag_name='publiclaw_wa_2_1' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
