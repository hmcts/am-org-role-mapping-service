package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FlagEventListener {

    @Autowired
    private final LDClient ldClient;

    @Autowired
    private FlagRefreshService flagRefreshService;

    public FlagEventListener(LDClient ldClient) {
        this.ldClient = ldClient;
    }
    //Auto event flow from LD
    public void logWheneverOneFlagChangesForOneUser( String flagKey, LDUser user) {
        if(ldClient !=null) {
            ldClient.getFlagTracker().addFlagValueChangeListener(flagKey, user, event -> {
                System.out.printf("Flag \"%s\" for user \"%s\" has changed from %s to %s\n", event.getKey(),
                        user.getKey(), event.getOldValue(), event.getNewValue()
                );
                if(event.getNewValue() != event.getOldValue()){
                    flagRefreshService.initRefreshJob();
                    //1) Check if orm-refresh-role flag is true on LD server. If yes then proceed with DB.
                    //
                    //2) Retrieve the DB lock so that another running node cannot intervene and insert the updated value in the DB
                    //3) Once all DB operation are committed successfully update the droolFlagStates map
                    Map<String,Boolean> droolFlagStates  =  LDFlagRegister.getDroolFlagStates();
                    droolFlagStates.put(event.getKey(),event.getNewValue().booleanValue());
                }
            });
        }
    }
}
