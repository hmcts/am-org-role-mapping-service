-- enable employment_wa_1_5 flag in Prod for: DTSAM-876 / DTSAM-861
update flag_config set status='true' where flag_name='employment_wa_1_5' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
