package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;

import javax.transaction.Transactional;

@Service
public class OrganisationService {

    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    private String activeOrgansationRefreshDays;

    public OrganisationService(OrganisationRefreshQueueRepository organisationRefreshQueueRepository,
            @Value("${professional.role.mapping.scheduling.organisationRefreshCleanup.activeOrgansationRefreshDays}")
            String activeOrgansationRefreshDays) {
        this.organisationRefreshQueueRepository = organisationRefreshQueueRepository;
        this.activeOrgansationRefreshDays = activeOrgansationRefreshDays;
    }


    @Transactional
    public void deleteActiveOrganisationRefreshRecords() {
        organisationRefreshQueueRepository
            .deleteActiveOrganisationRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(activeOrgansationRefreshDays);

    }
}
