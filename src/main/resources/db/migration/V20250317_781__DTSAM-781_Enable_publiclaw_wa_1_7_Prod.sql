-- enable publiclaw_wa_1_7 flag in Prod for: DTSAM-781 / DTSAM-777
update flag_config set status='true' where flag_name='publiclaw_wa_1_7' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
