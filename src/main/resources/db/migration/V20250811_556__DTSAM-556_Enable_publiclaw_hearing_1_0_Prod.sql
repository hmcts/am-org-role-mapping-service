-- enable publiclaw_hearing_1_0 flag in Prod for: DTSAM-556 / DTSAM-552
update flag_config set status='true' where flag_name='publiclaw_hearing_1_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
