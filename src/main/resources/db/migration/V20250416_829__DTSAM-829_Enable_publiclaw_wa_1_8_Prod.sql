-- enable publiclaw_wa_1_8 flag in Prod for: DTSAM-829 / DTSAM-828
update flag_config set status='true' where flag_name='publiclaw_wa_1_8' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
