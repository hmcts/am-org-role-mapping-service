package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.TestData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.dto.AccessTypeString;
import uk.gov.hmcts.reform.orgrolemapping.dto.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.dto.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.dto.OrganisationProfiles;
import uk.gov.hmcts.reform.orgrolemapping.dto.Role;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.data.TestData.CIVIL_JURISDICTION;
import static uk.gov.hmcts.reform.orgrolemapping.data.TestData.SSCS_JURISDICTION;

@Transactional
public class CaseDefinitionServiceIntegrationTest extends BaseTestIntegration {

    @Autowired
    private CaseDefinitionService caseDefinitionService;
    @Autowired
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Autowired
    private AccessTypesRepository accessTypesRepository;
    @MockBean
    private CCDService ccdService;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql"})
    void shouldUpdateLocalDefinitionsForSSCS() {

        ResponseEntity<AccessTypesResponse> ccdDefinitions = TestData.setupTestData(SSCS_JURISDICTION);

        when(ccdService.fetchAccessTypes()).thenReturn(ccdDefinitions);

        caseDefinitionService.findAndUpdateCaseDefinitionChanges();

        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        assertNotNull(profileRefreshQueueEntities);
        verifyProfileRefreshQueue(profileRefreshQueueEntities, 1, 1,
                "SSCS_SOLICITOR_PROFILE");

        List<AccessTypesEntity> accessTypesEntityList = iterableToList(accessTypesRepository.findAll());
        assertNotNull(accessTypesEntityList);

        OrganisationProfiles organisationProfiles = convertIntoPojo(accessTypesEntityList);
        assertNotNull(organisationProfiles);

        List<OrganisationProfile> organisationProfileList = organisationProfiles.getOrganisationProfiles();
        assertNotNull(organisationProfileList);
        assertEquals(1, organisationProfileList.size());
        verifyOrganisationProfiles(organisationProfiles, "SSCS_SOLICITOR_PROFILE",
                SSCS_JURISDICTION);

        verify(ccdService, times(1))
                .fetchAccessTypes();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql"})
    void shouldUpdateLocalDefinitionsForCivil() {

        ResponseEntity<AccessTypesResponse> ccdDefinitions = TestData.setupTestData(CIVIL_JURISDICTION);

        when(ccdService.fetchAccessTypes()).thenReturn(ccdDefinitions);

        caseDefinitionService.findAndUpdateCaseDefinitionChanges();

        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        assertNotNull(profileRefreshQueueEntities);
        verifyProfileRefreshQueue(profileRefreshQueueEntities, 1, 1,
                "CIVIL_SOLICITOR_PROFILE");

        List<AccessTypesEntity> accessTypesEntityList = iterableToList(accessTypesRepository.findAll());
        assertNotNull(accessTypesEntityList);

        OrganisationProfiles organisationProfiles = convertIntoPojo(accessTypesEntityList);
        assertNotNull(organisationProfiles);

        List<OrganisationProfile> organisationProfileList = organisationProfiles.getOrganisationProfiles();
        assertNotNull(organisationProfileList);
        assertEquals(1, organisationProfileList.size());
        verifyOrganisationProfiles(organisationProfiles, "CIVIL_SOLICITOR_PROFILE",
                CIVIL_JURISDICTION);

        verify(ccdService, times(1))
                .fetchAccessTypes();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_civil_existing_access_types.sql",
                       "classpath:sql/insert_existing_profiles_refresh_queue.sql"})
    void shouldNotUpdateLocalDefinitionsForCivil() {

        ResponseEntity<AccessTypesResponse> ccdDefinitions = TestData.setupTestData(CIVIL_JURISDICTION);

        when(ccdService.fetchAccessTypes()).thenReturn(ccdDefinitions);

        caseDefinitionService.findAndUpdateCaseDefinitionChanges();

        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        assertNotNull(profileRefreshQueueEntities);
        verifyProfileRefreshQueue(profileRefreshQueueEntities, 1, 1,
                "CIVIL_SOLICITOR_PROFILE");

        List<AccessTypesEntity> accessTypesEntityList = iterableToList(accessTypesRepository.findAll());
        assertNotNull(accessTypesEntityList);

        OrganisationProfiles organisationProfiles = convertIntoPojo(accessTypesEntityList);
        assertNotNull(organisationProfiles);

        List<OrganisationProfile> organisationProfileList = organisationProfiles.getOrganisationProfiles();
        assertNotNull(organisationProfileList);
        assertEquals(1, organisationProfileList.size());
        verifyOrganisationProfiles(organisationProfiles, "CIVIL_SOLICITOR_PROFILE",
                CIVIL_JURISDICTION);

        verify(ccdService, times(1))
                .fetchAccessTypes();
    }

    private static <T> List<T> iterableToList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    private OrganisationProfiles convertIntoPojo(List<AccessTypesEntity> accessTypesEntities) {
        for (AccessTypesEntity accessTypesEntity : accessTypesEntities) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                OrganisationProfiles organisationProfiles = mapper
                        .readValue(accessTypesEntity.getAccessTypes(), OrganisationProfiles.class);
                return organisationProfiles;

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private void verifyProfileRefreshQueue(List<ProfileRefreshQueueEntity> profileRefreshQueueEntities,
                                           int expectedProfileRefreshQueue, int expectedMinVersion,
                                           String expectedOrgProfileId) {

        assertEquals(expectedProfileRefreshQueue, profileRefreshQueueEntities.size());
        for (ProfileRefreshQueueEntity refreshQueueEntity : profileRefreshQueueEntities) {
            assertEquals(expectedOrgProfileId, refreshQueueEntity.getOrganisationProfileId());
            assertEquals(expectedMinVersion, refreshQueueEntity.getAccessTypesMinVersion());
            assertTrue(refreshQueueEntity.getActive());
        }
    }


    private void verifyOrganisationProfiles(OrganisationProfiles organisationProfiles,
                                            String expectedOrgProfileId, String expectedJurisdictionId) {

        List<OrganisationProfile> organisationProfileList = organisationProfiles.getOrganisationProfiles();
        assertEquals(1, organisationProfileList.size());

        for (OrganisationProfile organisationProfile : organisationProfileList) {
            List<Jurisdiction> jurisdictions = organisationProfile.getJurisdictions();
            assertNotNull(jurisdictions);


            assertNotNull(organisationProfile.getOrganisationProfileId());
            assertEquals(expectedOrgProfileId, organisationProfile.getOrganisationProfileId());

            verifyJurisdiction(jurisdictions, expectedJurisdictionId);
        }
    }

    private void verifyJurisdiction(List<Jurisdiction> jurisdictions, String expectedJurisdictionId) {
        for (Jurisdiction jurisdiction : jurisdictions) {

            assertNotNull(jurisdiction.getJurisdictionId());
            assertEquals(expectedJurisdictionId, jurisdiction.getJurisdictionId());

            List<AccessTypeString> accessTypeStringList = jurisdiction.getAccessTypes();
            assertNotNull(accessTypeStringList);
            verifyAccessTypes(accessTypeStringList, expectedJurisdictionId);
        }

    }

    private void verifyAccessTypes(List<AccessTypeString> accessTypeStringList, String jurisdictionId) {
        for (AccessTypeString accessTypeString : accessTypeStringList) {
            assertNotNull(accessTypeString.getAccessTypeId());
            assertEquals(jurisdictionId + "_ACCESS_TYPE_ID", accessTypeString.getAccessTypeId());

            assertNotNull(accessTypeString.isAccessDefault());
            assertTrue(accessTypeString.isAccessDefault());

            assertTrue(accessTypeString.isAccessMandatory());
            assertTrue(accessTypeString.isAccessMandatory());

            List<Role> roles = accessTypeString.getRoles();
            assertNotNull(roles);
            verifyRoles(roles, jurisdictionId);
        }
    }

    private void verifyRoles(List<Role> roles, String jurisdictionId) {
        for (Role role : roles) {
            assertNotNull(role.getCaseTypeId());
            assertEquals(jurisdictionId + "_Case_TYPE", role.getCaseTypeId());

            assertNotNull(role.getGroupRoleName());
            assertEquals(jurisdictionId + "_Group_Role1", role.getGroupRoleName());

            assertFalse(role.isGroupAccessEnabled());

            assertNotNull(role.getCaseGroupIdTemplate());
            assertEquals(jurisdictionId + "_CaseType:[GrpRoleName1]:$ORGID$", role.getCaseGroupIdTemplate());

            assertNotNull(role.getOrganisationalRoleName());
            assertEquals(jurisdictionId + "_Org_Role1", role.getOrganisationalRoleName());

        }
    }
}
