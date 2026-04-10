-- TEMP disable possessions_wa_1_0 flag in PREVIEW for testing of POFCC-103/POFCC-113
update flag_config set status='false' where flag_name='possessions_wa_1_0' and env in ('pr');
