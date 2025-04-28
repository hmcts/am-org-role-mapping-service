-- enable civil_wa_2_3 flag in Demo/AAT for: COT-958
update flag_config set status='true' where flag_name='civil_wa_2_3' and env in ('demo', 'aat');
