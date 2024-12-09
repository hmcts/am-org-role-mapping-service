-- enable st_cic_wa_1_1 flag in Prod for: COT-929
update flag_config set status='true' where flag_name='st_cic_wa_1_1' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
