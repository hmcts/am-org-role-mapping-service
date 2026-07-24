-- enable employment_wa_3_0 flag in Prod for: DTSAM-1061 / DTSAM-970
update flag_config set status='true' where flag_name='employment_wa_3_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
