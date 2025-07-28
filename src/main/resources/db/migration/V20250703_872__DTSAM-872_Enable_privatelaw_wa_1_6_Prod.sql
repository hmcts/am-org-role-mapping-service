-- enable privatelaw_wa_1_6 flag in Prod for: DTSAM-872 / DTSAM-626, DTSAM-656 & DTSAM-853
update flag_config set status='true' where flag_name='privatelaw_wa_1_6' and env in ('demo', 'aat', 'perftest', 'ithc', 'prod');
