-- enable employment_wa_1_4 flag in Prod for: DTSAM-422 / DTSAM-409
update flag_config set status='true' where flag_name='employment_wa_1_4' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
