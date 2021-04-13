package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FeatureFlagListener {

    @Autowired
    private final LDClient ldClient;

    public FeatureFlagListener(LDClient ldClient) {
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
                    //1) Check if role-refresh-enabled flag is true and proceed with DB operation.
                    //2) Retrieve the DB lock so that another running node cannot intervene and insert the updated value in the DB
                    //3) Once all DB operation are committed successfully update the droolFlagStates map
                    Map<String,Boolean> droolFlagStates  =  LDEventListener.getDroolFlagStates();
                    droolFlagStates.put(event.getKey(),event.getNewValue().booleanValue());
                }
            });
        }
    }
}
