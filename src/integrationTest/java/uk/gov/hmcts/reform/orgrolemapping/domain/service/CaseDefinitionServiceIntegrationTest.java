package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.*;
import uk.gov.hmcts.reform.orgrolemapping.helper.AccessTypesBuilder;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Transactional
public class CaseDefinitionServiceIntegrationTest extends BaseTestIntegration {



    @Autowired
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Autowired
    private AccessTypesRepository accessTypesRepository;
    @Autowired
    private CCDService ccdService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    CaseDefinitionService caseDefinitionService = new CaseDefinitionService(
            ccdService,
            accessTypesRepository,
            profileRefreshQueueRepository,
            objectMapper
    );

    @Test
    @Sql(scripts = {"classpath:sql/insert_access_types.sql"})
    void shouldUpdateLocalDefinitions() throws JsonProcessingException {

        AccessTypeRole ccdRoles = buildAccessTypeRole("BEFTA_CASETYPE_1_1", "Role1", "Role1",
                "BEFTA_JURISDICTION_1:BEFTA_CaseType:[GrpRoleName1]:$ORGID$", false);

        AccessType ccdAccessType = buildAccessType("SOLICITOR_PROFILE", "BEFTA_SOLICITOR_1", true, true, true,
                "BEFTA bulk Solicitor Respondant for Org description", "BEFTA bulk Solicitor Respondant for Org hint", 3, List.of(ccdRoles));

        AccessTypeJurisdiction jurisdictions = buildJurisdictions("BEFTA_JURISDICTION_1", "BEFTA_JURISDICTION_1_OGD", List.of(ccdAccessType));

        AccessTypesResponse response = buildAccessTypesResponse(List.of(jurisdictions));

        ResponseEntity<AccessTypesResponse> ccdDefinitions = new ResponseEntity<>(response, HttpStatus.OK);

        when(ccdService.fetchAccessTypes()).thenReturn(ccdDefinitions);

        caseDefinitionService.findAndUpdateCaseDefinitionChanges();

        RestructuredAccessTypes restructuredCCDDefinitions = AccessTypesBuilder.restructureCcdAccessTypes(Objects.requireNonNull(ccdDefinitions.getBody()));

        AccessTypesEntity savedAccessTypes =buildAccessTypesEntity(1, objectMapper.writeValueAsString(restructuredCCDDefinitions)) ;

        when(accessTypesRepository.updateAccessTypesEntity(any())).thenReturn(savedAccessTypes);


        verify(accessTypesRepository, times(1))
                .getAccessTypesEntity();
        verify(ccdService, times(1))
                .fetchAccessTypes();
        verify(accessTypesRepository, times(1))
                .updateAccessTypesEntity(any());


    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_populated_access_types.sql"})
    void shouldNotUpdateLocalDefinitions() throws JsonProcessingException {


        AccessTypeRole ccdRoles = buildAccessTypeRole("CIVIL", "Role1", "[APPLICANTSOLICITORONE]",
                "CIVIL:all-cases:APPSOL1:$ORGID$", true);

        AccessType ccdAccessType = buildAccessType("SOLICITOR_ORG", "civil-cases-1", false, false, true,
                "BEFTA bulk Solicitor Respondant for Org description", "BEFTA bulk Solicitor Respondant for Org hint", 3, List.of(ccdRoles));

        AccessTypeJurisdiction jurisdictions = buildJurisdictions("CIVIL", "BEFTA_JURISDICTION_1_OGD", List.of(ccdAccessType));

        AccessTypesResponse response = buildAccessTypesResponse(List.of(jurisdictions));

        ResponseEntity<AccessTypesResponse> ccdDefinitions = new ResponseEntity<>(response, HttpStatus.OK);

        when(ccdService.fetchAccessTypes()).thenReturn(ccdDefinitions);

        AccessTypesEntity accessTypes = accessTypesRepository.getAccessTypesEntity();

        caseDefinitionService.findAndUpdateCaseDefinitionChanges();

        RestructuredAccessTypes restructuredCCDDefinitions = AccessTypesBuilder.restructureCcdAccessTypes(Objects.requireNonNull(ccdDefinitions.getBody()));

        AccessTypesEntity savedAccessTypes =buildAccessTypesEntity(1, objectMapper.writeValueAsString(restructuredCCDDefinitions)) ;

        when(accessTypesRepository.updateAccessTypesEntity(any())).thenReturn(savedAccessTypes);


        verify(accessTypesRepository, times(1))
                .getAccessTypesEntity();
        verify(ccdService, times(1))
                .fetchAccessTypes();
        verify(accessTypesRepository, times(0))
                .updateAccessTypesEntity(any());


    }

    private AccessTypesEntity buildAccessTypesEntity(long version, String accessTypes){

        return AccessTypesEntity.builder()
                .version(version)
                .accessTypes(accessTypes)
                .build();

    }

    private AccessTypesResponse buildAccessTypesResponse(List<AccessTypeJurisdiction> jurisdictions){

        return AccessTypesResponse.builder()
                .jurisdictions(jurisdictions)
                .build();
    }

    private AccessTypeJurisdiction buildJurisdictions(String jurisdictionId, String jurisdictionName, List<AccessType> accessTypes){

        return AccessTypeJurisdiction.builder()
                .jurisdictionId(jurisdictionId)
                .jurisdictionName(jurisdictionName)
                .accessTypes(accessTypes)
                .build();
    }

    private AccessType buildAccessType(String organisationProfileId, String accessTypeId, boolean accessMandatory, boolean accessDefault, boolean display,
                                       String description, String hint, Integer displayOrder, List<AccessTypeRole> roles){

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
                                               String caseGroupIdTemplate, boolean groupAccessEnabled){

        return AccessTypeRole.builder()
                .caseTypeId(caseTypeId)
                .organisationalRoleName(organisationalRoleName)
                .groupRoleName(groupRoleName)
                .caseGroupIdTemplate(caseGroupIdTemplate)
                .groupAccessEnabled(groupAccessEnabled)
                .build();

    }

}
