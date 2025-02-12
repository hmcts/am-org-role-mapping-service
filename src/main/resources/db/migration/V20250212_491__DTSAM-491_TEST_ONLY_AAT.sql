-- enable flags that are not enabled in AAT by flyway by default
update flag_config set status='true' where flag_name='iac_1_1' and env in ('aat');
update flag_config set status='true' where flag_name='iac_jrd_1_0' and env in ('aat');
update flag_config set status='true' where flag_name='sscs_hearing_1_0' and env in ('aat');
update flag_config set status='true' where flag_name='civil_wa_1_0' and env in ('aat');
update flag_config set status='true' where flag_name='privatelaw_wa_1_0' and env in ('aat');
update flag_config set status='true' where flag_name='iac_wa_1_3' and env in ('aat');
