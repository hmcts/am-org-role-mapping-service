package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagState;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagStateRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJob;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class FlagRefreshService {

    private FlagStateRepository flagStateRepository;
    private RefreshJobsRepository refreshJobsRepository;
    private FeatureToggleService  featureToggleService;

    public FlagRefreshService(FlagStateRepository flagStateRepository, RefreshJobsRepository refreshJobsRepository, FeatureToggleService featureToggleService) {
        this.flagStateRepository = flagStateRepository;
        this.refreshJobsRepository = refreshJobsRepository;
        this.featureToggleService = featureToggleService;
    }

    public void initFlagState(FlagConfig flagConfig){

        //1) This is first time flag value being read and inserted in the DB, hence no need to check orm-refresh-role flag.
        // First check if the flag already exist in the table, if yes then skip. If No then insert with default config value.
        // If inserting, then retrieve the exclusive lock on the table, so that other nodes cannot insert the same entry.
        // insert the flag
        Optional<FlagState>  flagState = flagStateRepository.findById(flagConfig.getName());
        if(!flagState.isPresent()){
        FlagState flagStateEntity  =  FlagState.builder()
                    .actorId(flagConfig.getName())
                    .state(flagConfig.getDefaultValue())
                    .build();
            flagStateRepository.save(flagStateEntity);

            // 2) If DB operation executed successfully, then add the flag with default status in the static map as well.
            Map<String,Boolean> droolFlagStates  =  LDFlagRegister.getDroolFlagStates();
            droolFlagStates.put(flagConfig.getName(),flagConfig.getDefaultValue());

        }



    }

    public void initRefreshJob(){
      if(featureToggleService.isFlagEnabled("orm-refresh-role")){
          //fetch all the register flag from map
          Map<String,Boolean> droolFlagStates  =  LDFlagRegister.getDroolFlagStates();
          droolFlagStates.forEach((K,V)->{
              boolean newValue = featureToggleService.isFlagEnabled(K);
              Optional<FlagState>  flagState = flagStateRepository.findById(K);
              if(flagState.isPresent()){
                boolean oldValue  = flagState.get().getState();
                //if newValue is different then insert new value and it's job from config
               //this operation should be atomic and isolation mode.
                if(oldValue != newValue) {
                    flagState.get().setState(newValue);
                    flagStateRepository.save(flagState.get());

                    //Insert into refresh job
                    FlagConfigs.getFlagConfigs().get(K).getRefresh().forEach(job->{
                            RefreshJob refreshJob  =RefreshJob.builder()
                            .jurisdiction(job.getJurisdiction())
                            .userType(job.getRoleCategory())
                            .status("NEW")
                            .build();
                        refreshJobsRepository.save(refreshJob);
                });

                    //update the map
                    droolFlagStates.put(K,newValue);

                }
              }

          });


      }


    }
}
