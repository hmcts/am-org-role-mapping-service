-- enable publiclaw_wa_2_0 flag in Prod for: DTSAM-917 / DTSAM-826
update flag_config set status='true' where flag_name='publiclaw_wa_2_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
