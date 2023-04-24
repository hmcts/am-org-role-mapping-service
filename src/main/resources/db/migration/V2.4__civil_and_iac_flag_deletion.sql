-- AM-2521: Remove old unused DB flags for IAC and SSCS

delete from flag_config where flag_name='iac_1_1';
delete from flag_config where flag_name='iac_jrd_1_0';
delete from flag_config where flag_name='sscs_hearing_1_0';