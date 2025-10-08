-- enable st_cic_wa_1_0 flag in Prod for: DTSAM-523 / DTSAM-519
update flag_config set status='true' where flag_name='st_cic_wa_1_0' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
