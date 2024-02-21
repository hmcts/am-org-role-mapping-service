package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql"})
    void shouldUpdateLocalDefinitions() throws JsonProcessingException {


        AccessTypeRole ccdRoles = buildAccessTypeRole("BEFTA_CASETYPE_1_1", "Role1", "Role1",
                "BEFTA_JURISDICTION_1:BEFTA_CaseType:[GrpRoleName1]:$ORGID$", false);

        AccessType ccdAccessType = buildAccessType("SOLICITOR_PROFILE", "BEFTA_SOLICITOR_1",
                true, true, true,
                "BEFTA bulk Solicitor Respondant for Org description",
                "BEFTA bulk Solicitor Respondant for Org hint", 3, List.of(ccdRoles));

        AccessTypeJurisdiction jurisdictions =
                buildJurisdictions("BEFTA_JURISDICTION_1", "BEFTA_JURISDICTION_1_OGD", List.of(ccdAccessType));

        AccessTypesResponse response = buildAccessTypesResponse(List.of(jurisdictions));

        ResponseEntity<AccessTypesResponse> ccdDefinitions = new ResponseEntity<>(response, HttpStatus.OK);

        when(ccdService.fetchAccessTypes()).thenReturn(ccdDefinitions);

        caseDefinitionService.findAndUpdateCaseDefinitionChanges();


        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();

        ProfileRefreshQueueEntity profileRefreshQueueEntity = profileRefreshQueueEntities.get(0);

        assertEquals(profileRefreshQueueEntities.size(), 1);

        assertNotNull(profileRefreshQueueEntities.get(0));

        assertEquals(profileRefreshQueueEntity.getOrganisationProfileId(), "SOLICITOR_PROFILE");

        assertEquals(profileRefreshQueueEntity.getAccessTypesMinVersion(), 2);

        verify(ccdService, times(1))
                .fetchAccessTypes();

    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_populated_access_types.sql"})
    void shouldNotUpdateLocalDefinitions() throws JsonProcessingException {


        AccessTypeRole ccdRoles = buildAccessTypeRole("CIVIL", "Role1", "[APPLICANTSOLICITORONE]",
                "CIVIL:all-cases:APPSOL1:$ORGID$", true);

        AccessType ccdAccessType = buildAccessType("SOLICITOR_ORG", "civil-cases-1",
                false, false, true,
                "BEFTA bulk Solicitor Respondant for Org description",
                "BEFTA bulk Solicitor Respondant for Org hint", 3, List.of(ccdRoles));

        AccessTypeJurisdiction jurisdictions =
                buildJurisdictions("CIVIL", "BEFTA_JURISDICTION_1_OGD", List.of(ccdAccessType));

        AccessTypesResponse response = buildAccessTypesResponse(List.of(jurisdictions));

        ResponseEntity<AccessTypesResponse> ccdDefinitions = new ResponseEntity<>(response, HttpStatus.OK);

        when(ccdService.fetchAccessTypes()).thenReturn(ccdDefinitions);

        caseDefinitionService.findAndUpdateCaseDefinitionChanges();


        List<ProfileRefreshQueueEntity> profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();

        assertEquals(profileRefreshQueueEntities.size(), 0);

        verify(ccdService, times(1))
                .fetchAccessTypes();
    }

    private AccessTypesEntity buildAccessTypesEntity(long version, String accessTypes) {

        return AccessTypesEntity.builder()
                .version(version)
                .accessTypes(accessTypes)
                .build();

    }

    private AccessTypesResponse buildAccessTypesResponse(List<AccessTypeJurisdiction> jurisdictions) {

        return AccessTypesResponse.builder()
                .jurisdictions(jurisdictions)
                .build();

    }

    private AccessTypeJurisdiction buildJurisdictions(String jurisdictionId,
                                                      String jurisdictionName,
                                                      List<AccessType> accessTypes) {

        return AccessTypeJurisdiction.builder()
                .jurisdictionId(jurisdictionId)
                .jurisdictionName(jurisdictionName)
                .accessTypes(accessTypes)
                .build();

    }

    private AccessType buildAccessType(String organisationProfileId, String accessTypeId, boolean accessMandatory,
                                       boolean accessDefault, boolean display, String description,
                                       String hint, Integer displayOrder, List<AccessTypeRole> roles) {

        return AccessType.builder()
                .organisationProfileId(organisationProfileId)
                .accessTypeId(accessTypeId)
                .accessMandatory(accessMandatory)
                .accessDefault(accessDefault)
                .display(display)
                .description(description)
                .hint(hint)
                .displayOrder(displayOrder)
                .roles(roles)
                .build();

    }

    private AccessTypeRole buildAccessTypeRole(String caseTypeId, String organisationalRoleName, String groupRoleName,
                                               String caseGroupIdTemplate, boolean groupAccessEnabled) {

        return AccessTypeRole.builder()
                .caseTypeId(caseTypeId)
                .organisationalRoleName(organisationalRoleName)
                .groupRoleName(groupRoleName)
                .caseGroupIdTemplate(caseGroupIdTemplate)
                .groupAccessEnabled(groupAccessEnabled)
                .build();

    }

}
