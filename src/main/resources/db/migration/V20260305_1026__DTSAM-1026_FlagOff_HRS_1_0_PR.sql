-- enable hrs_1_0 flag in Prod for: DTSAM-1026 
update flag_config set status='false' where flag_name='hrs_1_0' and env in ('pr');
