-- TEMP disable civil_wa_2_3 flag in PREVIEW for testing of COT-958
update flag_config set status='false' where flag_name='civil_wa_2_3' and env in ('pr');
