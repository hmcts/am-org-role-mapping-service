-- AM-2790: WA R4 Enabling Flags for Refresh

-- enable CIVIL flag for: AM-2730 - Prod Civil role issue - Any user with a team leader role does not inherit the permissions of the staff role that it has responsibility for  
update flag_config set status='true' where flag_name='civil_wa_1_1' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');


-- enable IAC flag for: AM-2732 - CA & TS new role mapping for IAC - CTSC
update flag_config set status='true' where flag_name='iac_wa_1_2' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');


-- enable PRIVATELAW flag for: AM-2755 - Add new worktype for Tribunal Caseworker in Private Law
update flag_config set status='true' where flag_name='privatelaw_wa_1_1' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
