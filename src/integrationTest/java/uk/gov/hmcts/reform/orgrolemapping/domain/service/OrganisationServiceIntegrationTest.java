package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
public class OrganisationServiceIntegrationTest extends BaseTestIntegration {

    @Autowired
    OrganisationService organisationService;

    @Autowired
    OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_new_organisation_profiles.sql"})
    void shouldDeleteNoneFromUserRefreshQueueTest() {
        organisationService.deleteActiveOrganisationRefreshRecords();

        assertEquals(1, organisationRefreshQueueRepository.findAll().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_old_organisation_profiles.sql"})
    void shouldDeleteOneFromUserRefreshQueueTest() {
        organisationService.deleteActiveOrganisationRefreshRecords();

        assertEquals(0, organisationRefreshQueueRepository.findAll().size());
    }
}
