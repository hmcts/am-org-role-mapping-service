package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.helper.AccessTypesBuilder;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AccessTypesBuilderTest.buildOrganisationProfile;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AccessTypesBuilderTest.buildOrganisationProfileAccessType;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AccessTypesBuilderTest.buildRestructuredAccessTypes;

@ExtendWith(MockitoExtension.class)
class CaseDefinitionServiceTest {

    private final CCDService ccdService = Mockito.mock(CCDService.class);
    private final AccessTypesRepository accessTypesRepository = Mockito.mock(AccessTypesRepository.class);
    private final ProfileRefreshQueueRepository profileRefreshQueueRepository
            = Mockito.mock(ProfileRefreshQueueRepository.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ProcessEventTracker processEventTracker = Mockito.mock(ProcessEventTracker.class);

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    private final AccessTypesBuilder accessTypesBuilder = new AccessTypesBuilder();

    CaseDefinitionService caseDefinitionService = new CaseDefinitionService(
            ccdService,
            accessTypesRepository,
            profileRefreshQueueRepository,
            objectMapper,
            processEventTracker
     );

    @Test
    void findAndUpdateCaseDefinitionChanges_shouldUpdate() throws IOException {

        OrganisationProfileAccessType accessType1 = buildOrganisationProfileAccessType("accessTypeId1", true, true,
                "caseTypeId1", "orgRoleName1", "groupRoleName1", "caseGroupTemplate1", true);

        RestructuredAccessTypes prmRestructuredAccessTypes = buildRestructuredAccessTypes(
                Set.of(
                        buildOrganisationProfile("SOLICITOR_ORG", "CIVIL", Set.of(accessType1))
                )
        );

        AccessTypesEntity localDefinitions =
                buildAccessTypesEntity(1, objectMapper.writeValueAsString(prmRestructuredAccessTypes));

        when(accessTypesRepository.getAccessTypesEntity()).thenReturn(localDefinitions);

        AccessTypeRole ccdRoles = buildAccessTypeRole("BEFTA_CASETYPE_1_1", "Role1", "Role1",
                "BEFTA_JURISDICTION_1:BEFTA_CaseType:[GrpRoleName1]:$ORGID$", false);

        AccessType ccdAccessType = buildAccessType("SOLICITOR_PROFILE",
                "BEFTA_SOLICITOR_1", true, true, true,
                "BEFTA bulk Solicitor Respondant for Org description",
                "BEFTA bulk Solicitor Respondant for Org hint", 3, List.of(ccdRoles));

        AccessTypeJurisdiction jurisdictions
                = buildJurisdictions("BEFTA_JURISDICTION_1", "BEFTA_JURISDICTION_1_OGD", List.of(ccdAccessType));

        AccessTypesResponse response = buildAccessTypesResponse(List.of(jurisdictions));

        ResponseEntity<AccessTypesResponse> ccdDefinitions = new ResponseEntity<>(response, HttpStatus.OK);

        when(ccdService.fetchAccessTypes()).thenReturn(ccdDefinitions);

        RestructuredAccessTypes restructuredCCDDefinitions
                = accessTypesBuilder.restructureCcdAccessTypes(Objects.requireNonNull(ccdDefinitions.getBody()));

        AccessTypesEntity savedAccessTypes =
                buildAccessTypesEntity(1, objectMapper.writeValueAsString(restructuredCCDDefinitions));

        when(accessTypesRepository.updateAccessTypesEntity(any())).thenReturn(savedAccessTypes);

        caseDefinitionService.findAndUpdateCaseDefinitionChanges();

        verify(accessTypesRepository, times(1))
                .getAccessTypesEntity();
        verify(ccdService, times(1))
                .fetchAccessTypes();
        verify(accessTypesRepository, times(1))
                .updateAccessTypesEntity(any());

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);

    }

    @Test
    void findAndUpdateCaseDefinitionChanges_shouldNotUpdate() throws IOException {

        OrganisationProfileAccessType accessType1 = buildOrganisationProfileAccessType("BEFTA_SOLICITOR_1", true, true,
                "BEFTA_CASETYPE_1_1", "Role1", "Role1",
                "BEFTA_JURISDICTION_1:BEFTA_CaseType:[GrpRoleName1]:$ORGID$", false);

        RestructuredAccessTypes prmRestructuredAccessTypes = buildRestructuredAccessTypes(
                Set.of(
                        buildOrganisationProfile("SOLICITOR_PROFILE", "BEFTA_JURISDICTION_1", Set.of(accessType1))
                )
        );

        AccessTypesEntity localDefinitions =
                buildAccessTypesEntity(1, objectMapper.writeValueAsString(prmRestructuredAccessTypes));

        when(accessTypesRepository.getAccessTypesEntity()).thenReturn(localDefinitions);

        AccessTypeRole ccdRoles = buildAccessTypeRole("BEFTA_CASETYPE_1_1", "Role1", "Role1",
                "BEFTA_JURISDICTION_1:BEFTA_CaseType:[GrpRoleName1]:$ORGID$", false);

        AccessType ccdAccessType = buildAccessType("SOLICITOR_PROFILE", "BEFTA_SOLICITOR_1",
                true, true, true, "BEFTA bulk Solicitor Respondant for Org description",
                "BEFTA bulk Solicitor Respondant for Org hint", 3, List.of(ccdRoles));

        AccessTypeJurisdiction jurisdictions =
                buildJurisdictions("BEFTA_JURISDICTION_1", "BEFTA_JURISDICTION_1_OGD", List.of(ccdAccessType));

        AccessTypesResponse response = buildAccessTypesResponse(List.of(jurisdictions));

        ResponseEntity<AccessTypesResponse> ccdDefinitions = new ResponseEntity<>(response, HttpStatus.OK);

        when(ccdService.fetchAccessTypes()).thenReturn(ccdDefinitions);

        RestructuredAccessTypes restructuredCCDDefinitions =
                accessTypesBuilder.restructureCcdAccessTypes(Objects.requireNonNull(ccdDefinitions.getBody()));

        AccessTypesEntity savedAccessTypes =
                buildAccessTypesEntity(1, objectMapper.writeValueAsString(restructuredCCDDefinitions));

        when(accessTypesRepository.updateAccessTypesEntity(any())).thenReturn(savedAccessTypes);

        caseDefinitionService.findAndUpdateCaseDefinitionChanges();

        verify(accessTypesRepository, times(1))
                .getAccessTypesEntity();
        verify(ccdService, times(1))
                .fetchAccessTypes();
        verify(accessTypesRepository, times(0))
                .updateAccessTypesEntity(any());

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);

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

    private AccessTypeJurisdiction buildJurisdictions(
            String jurisdictionId, String jurisdictionName, List<AccessType> accessTypes) {

        return AccessTypeJurisdiction.builder()
                .jurisdictionId(jurisdictionId)
                .jurisdictionName(jurisdictionName)
                .accessTypes(accessTypes)
                .build();

    }

    private AccessType buildAccessType(String organisationProfileId, String accessTypeId,
                                       boolean accessMandatory, boolean accessDefault,
                                       boolean display, String description, String hint,
                                       Integer displayOrder, List<AccessTypeRole> roles) {

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
