package uk.gov.hmcts.reform.orgrolemapping.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RoleAssignmentService;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class RefreshPreviewConfig implements CommandLineRunner {

    private final Integer rasHealthCount;
    private final Integer rasHealthInterval;
    private final Boolean caseworkerEnabled;
    private final Boolean judicialEnabled;
    private final List<String> caseworkerUsers;
    private final List<String> judicialUsers;
    private final List<String> caseworkerJurisdictions;
    private final List<String> judicialJurisdictions;

    private final EnvironmentConfiguration environmentConfiguration;
    private final String rasURL;

    private RoleAssignmentService roleAssignmentService;


    @Autowired
    public RefreshPreviewConfig(@Value("${refresh.job.preview.rasHealth.update.count}") Integer rasHealthCount,
                                @Value("${refresh.job.preview.rasHealth.update.interval}") Integer rasHealthInterval,
                                @Value("${refresh.job.preview.caseworker.enabled}") Boolean caseworkerEnabled,
                                @Value("${refresh.job.preview.judicial.enabled}") Boolean judicialEnabled,
                                @Value("${refresh.job.preview.caseworker.users}") String caseworkerUsers,
                                @Value("${refresh.job.preview.judicial.users}") String judicialUsers,
                                @Value("${refresh.job.preview.caseworker.jurisdictions}") String caseworkerJurisdictions,
                                @Value("${refresh.job.preview.judicial.jurisdictions}") String judicialJurisdictions,
                                EnvironmentConfiguration environmentConfiguration ,
                                @Value("${feign.client.config.roleAssignmentApp.url}") String rasURL,
                                RoleAssignmentService roleAssignmentService) {
        this.rasHealthCount = rasHealthCount;
        this.rasHealthInterval = rasHealthInterval;
        this.caseworkerEnabled = caseworkerEnabled;

        this.judicialEnabled = judicialEnabled;
        this.caseworkerUsers = csvToList(caseworkerUsers);
        this.judicialUsers = csvToList(judicialUsers);
        this.caseworkerJurisdictions = csvToList(caseworkerJurisdictions);
        this.judicialJurisdictions = csvToList(judicialJurisdictions);
        this.environmentConfiguration = environmentConfiguration;
        this.rasURL = rasURL;
        this.roleAssignmentService = roleAssignmentService;
    }

    private List<String> csvToList(String csv){
        if (StringUtils.isNotEmpty(csv)) {
            return Arrays.stream(csv.split(",")).toList();
        }
        return List.of();
    }

    @Override
    public void run(String... args) {
        //This needs to run only on preview
        if (environmentConfiguration.getEnvironment().equalsIgnoreCase("pr")) {
            if (judicialEnabled || caseworkerEnabled) {
                if (rasURL.contains("aat")) {
                   log.error("The Refresh is not permitted in AAT");
                } else if(isRasHealthy()) {
                    log.info("RAS is healty ");
                    //TODO call the new function
                }
            }
        }
    }

    private Boolean isRasHealthy(){
        final int maxRetries = 10;

        for (int i = 0; i < maxRetries; i++) {
            String status = roleAssignmentService.getServiceStatus();

            if ("UP".equalsIgnoreCase(status)) {
                return true;
            }

            log.info("Service is not UP. Current status: {}. Retry {}/{}", status, i + 1, maxRetries);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Polling interrupted", e);
                return false;
            }
        }

        return false;
    }

    //fucntion takes rolecategory and userid List , jurisdiction list
    //null check
    // caseworker and judicial return job id
    //RefreshJobEntity
    //createRefreshJob for each juridictions
    //    RefreshJobEntity newJob = RefreshJobEntity.builder()
    //                .jurisdiction(jurisdiction)
    //                .roleCategory(roleCategory)
    //                .status("NEW")
    //                .userIds( null)
    //                .linkedJobId(0)//null
    //                .comments(""created from preview Refresh"")
    //                .created(ZonedDateTime.now())
    //                .build();
    //        newJob = persistenceService.persistRefreshJob(newJob);
    //List.add(newJob)
    //if userId list not empty

    //RefreshJobEntity newJob = RefreshJobEntity.builder()
    //                .jurisdiction(jurisdiction)
    //                .roleCategory(roleCategory)
    //                .status("NEW")
    //                .userIds( UserIds().toArray(new String[0]) : null)
    //                .linkedJobId(0)
    //                .comments("TODO")
    //                .created(ZonedDateTime.now())
    //                .build();
    //        newJob = persistenceService.persistRefreshJob(newJob);
    //            newJob.setLinkedJobId(newJob.getJobId());
    //            newJob = persistenceService.persistRefreshJob(newJob);
    // List.add(newJob)
    //Call this in interval as this is async get from configuration
    //call for each job RefreshOrchestrator.refreshAsync
    //public void refreshAsync(Long jobId, UserRequest userRequest)

}
