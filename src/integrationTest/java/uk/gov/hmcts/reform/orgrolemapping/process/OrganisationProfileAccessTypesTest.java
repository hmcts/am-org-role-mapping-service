package uk.gov.hmcts.reform.orgrolemapping.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.util.AccessTypeManipulation;
import uk.gov.hmcts.reform.orgrolemapping.util.TypeOfCompare;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildAccessTypesResponse;

class OrganisationProfileAccessTypesTest extends BaseTestIntegration {

    @Autowired
    AccessTypesRepository accessTypesRepository;

    private AccessTypesEntity accessTypesEntityBefore;

    private List<String> organisationProfileIdsAccessTypedefinitionsModified = null;

    public OrganisationProfileAccessTypesTest() {
    }

    @BeforeEach
    void init() {
        accessTypesRepository.deleteAll();
        accessTypesRepository.flush();
        setUp();
    }

    void setUp() {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .accessTypes("{}")
                .build();
        AccessTypesEntity resp = accessTypesRepository.saveAndFlush(accessTypesEntity);
        accessTypesEntityBefore = accessTypesRepository.findAll().stream().findFirst().orElse(null);
        String accessTypesBeforeJson = accessTypesEntityBefore.getAccessTypes();
        assertThat(accessTypesRepository.findAll().stream().findFirst().orElse(null)).isNotNull();
        assertThat(accessTypesBeforeJson).isNotNull();
    }

    @Test
    void comparingSavedWithRecievedAccessTypes_shouldChangeDB() {
        try {
            AccessTypesResponse accessTypesResponse  = buildAccessTypesResponse();

            List<AccessTypeJurisdiction> jurisdictions = accessTypesResponse.getJurisdictions();
            OrganisationProfileAccessTypes organisationProfile =
                    new OrganisationProfileAccessTypes(accessTypesRepository,accessTypesResponse);
            organisationProfileIdsAccessTypedefinitionsModified =
                    organisationProfile.getOrganisationProfileIdsAccessTypedefinitionsModified();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String accessTypesBeforeJson = accessTypesEntityBefore.getAccessTypes();
        Long versionBefore = accessTypesEntityBefore.getVersion();

        AccessTypesEntity accessTypesAfter = accessTypesRepository.findAll().stream().findFirst().orElse(null);
        Long versionAfter = accessTypesAfter.getVersion();
        String accessTypesAfterJson = accessTypesAfter.getAccessTypes();
        assertNotNull(accessTypesAfter);
        assertNotEquals(versionBefore, versionAfter);

        assertNotEquals(accessTypesBeforeJson, accessTypesAfterJson);
        assertNotNull(organisationProfileIdsAccessTypedefinitionsModified);
        assertEquals(true, !organisationProfileIdsAccessTypedefinitionsModified.isEmpty());

        String organisationProfileId = organisationProfileIdsAccessTypedefinitionsModified.get(0);

        assertEquals("SOLICITOR_PROFILE", organisationProfileId);
    }

    @Test
    void comparingSavedWithChangedRecievedAccessTypes_ShouldChangeDB() {
        try {
            AccessTypesResponse accessTypesResponse  = buildAccessTypesResponse();
            List<AccessTypeJurisdiction> jurisdictions = accessTypesResponse.getJurisdictions();
            jurisdictions.get(0).setJurisdictionId("BEFTA_JURISDICTION_2");
            jurisdictions.get(0).setJurisdictionName("BEFTA_JURISDICTION_2");

            OrganisationProfileAccessTypes organisationProfile =
                    new OrganisationProfileAccessTypes(accessTypesRepository,accessTypesResponse);
            organisationProfileIdsAccessTypedefinitionsModified =
                    organisationProfile.getOrganisationProfileIdsAccessTypedefinitionsModified();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Long versionBefore = accessTypesEntityBefore.getVersion();
        Long versionAfter;
        AccessTypesEntity accessTypesAfter = accessTypesRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(accessTypesAfter);

        versionAfter = accessTypesAfter.getVersion();
        assertNotEquals(versionBefore, versionAfter);

        String accessTypesBeforeJson = accessTypesEntityBefore.getAccessTypes();
        String accessTypesAfterJson = accessTypesAfter.getAccessTypes();
        //Expected :{}
        //Actual   :[{"jurisdictions": [{"access_types": [{"roles": [{"caseTypeId": "CASE_TYPE_WITH_NO_CASES",
        // "groupRoleName": null, "groupAccessEnabled": false,
        // "caseGroupIdTemplate": "BEFTA_JURISDICTION_1:CIVIL:all:CIVIL:AS1:$ORGID$",
        // "organisationalRoleName": "Role1 ...
        assertNotEquals(accessTypesBeforeJson, accessTypesAfterJson);
        assertNotNull(organisationProfileIdsAccessTypedefinitionsModified);
        assertEquals(true, !organisationProfileIdsAccessTypedefinitionsModified.isEmpty());

        String organisationProfileId = organisationProfileIdsAccessTypedefinitionsModified.get(0);

        assertEquals("SOLICITOR_PROFILE", organisationProfileId);
    }

    @Test
    void mismatchedExceptionWhenComparingSavedEmptyWithRecievedAccessTypes_ShouldChangeDB() {

        try {
            AccessTypesResponse accessTypesResponse  = buildAccessTypesResponse();
            OrganisationProfileAccessTypes organisationProfile =
                    new OrganisationProfileAccessTypes(accessTypesRepository,accessTypesResponse);
            organisationProfileIdsAccessTypedefinitionsModified =
                    organisationProfile.getOrganisationProfileIdsAccessTypedefinitionsModified();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Long versionAfter;
        AccessTypesEntity accessTypesAfter = accessTypesRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(accessTypesAfter);
        versionAfter = accessTypesAfter.getVersion();
        String accessTypesAfterJson = accessTypesAfter.getAccessTypes();

        String accessTypesBeforeJson = accessTypesEntityBefore.getAccessTypes();
        Long versionBefore = accessTypesEntityBefore.getVersion();

        assertNotEquals(versionBefore, versionAfter);

        assertNotEquals(accessTypesBeforeJson, accessTypesAfterJson);

        assertNotNull(organisationProfileIdsAccessTypedefinitionsModified);
        assertEquals(true, !organisationProfileIdsAccessTypedefinitionsModified.isEmpty());
        String organisationProfileId = organisationProfileIdsAccessTypedefinitionsModified.get(0);

        assertEquals("SOLICITOR_PROFILE", organisationProfileId);
    }

    @Test
    void comparingSavedRecievedAccessTypesWithRecievedAccessTypes_ShouldNotChangeDB() {

        try {
            initSaveAccessTypeEntityReceived();

            AccessTypesResponse accessTypesResponse  = buildAccessTypesResponse();
            OrganisationProfileAccessTypes organisationProfile =
                    new OrganisationProfileAccessTypes(accessTypesRepository,accessTypesResponse);
            organisationProfileIdsAccessTypedefinitionsModified =
                    organisationProfile.getOrganisationProfileIdsAccessTypedefinitionsModified();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertNotNull(accessTypesEntityBefore);

        String accessTypesBeforeJson = accessTypesEntityBefore.getAccessTypes();
        Long versionBefore = accessTypesEntityBefore.getVersion();

        Long versionAfter;
        AccessTypesEntity accessTypesAfter = accessTypesRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(accessTypesAfter);
        versionAfter = accessTypesAfter.getVersion();
        assertEquals(versionBefore, versionAfter);

        assertNull(organisationProfileIdsAccessTypedefinitionsModified);

    }

    @Test
    void comparingSavedRecievedAccessTypesWithRecievedAccessTypes_ShouldNotChangeDB_TypeOfCompare() {

        try {
            initSaveAccessTypeEntityReceived();

            AccessTypesResponse accessTypesResponse  = buildAccessTypesResponse();
            OrganisationProfileAccessTypes organisationProfile =
                    new OrganisationProfileAccessTypes(accessTypesRepository,accessTypesResponse, TypeOfCompare.SET);
            organisationProfileIdsAccessTypedefinitionsModified =
                    organisationProfile.getOrganisationProfileIdsAccessTypedefinitionsModified();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertNotNull(accessTypesEntityBefore);

        String accessTypesBeforeJson = accessTypesEntityBefore.getAccessTypes();
        Long versionBefore = accessTypesEntityBefore.getVersion();

        Long versionAfter;
        AccessTypesEntity accessTypesAfter = accessTypesRepository.findAll().stream().findFirst().orElse(null);
        assertNotNull(accessTypesAfter);
        versionAfter = accessTypesAfter.getVersion();
        assertEquals(versionBefore, versionAfter);

        assertNull(organisationProfileIdsAccessTypedefinitionsModified);

    }

    private void initSaveAccessTypeEntityReceived() throws IOException {
        accessTypesRepository.deleteAll();
        accessTypesRepository.flush();

        AccessTypesResponse accessTypesResponse  = buildAccessTypesResponse();
        AccessTypeManipulation accessTypeManipulation = new AccessTypeManipulation();
        List<OrganisationProfile> organisationProfile = accessTypeManipulation
                .restructureToOrganisationProfiles(accessTypesResponse.getJurisdictions());
        String jsonString = accessTypeManipulation.organisationProfileToJsonString(organisationProfile);
        AccessTypesEntity accessTypesEntityToSave = new AccessTypesEntity();
        accessTypesEntityToSave.setAccessTypes(jsonString);
        accessTypesEntityToSave.setVersion(accessTypesRepository.getNextAccessTypeVersion());
        AccessTypesEntity accessTypesEntitySaved = accessTypesRepository.saveAndFlush(accessTypesEntityToSave);

        AccessTypesEntity accessTypesEntityGetSaved = accessTypesRepository.findAll().stream().findFirst()
                .orElse(null);
        String accessTypesSaved = accessTypesEntityGetSaved.getAccessTypes();

        accessTypesEntityBefore = accessTypesRepository.findAll().stream().findFirst().orElse(null);

        assertThat(accessTypesEntityBefore).isNotNull();
        assertThat(accessTypesSaved).isNotNull();
    }
}