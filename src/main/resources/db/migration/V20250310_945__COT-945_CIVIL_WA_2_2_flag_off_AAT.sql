-- TEMP disable civil_wa_2_2 flag in PREVIEW for testing of COT-945
update flag_config set status='false' where flag_name='civil_wa_2_2' and env in ('pr');
