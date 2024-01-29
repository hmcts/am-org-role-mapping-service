package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileJurisdiction;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessTypesBuilderTest {

    static Stream<RestructuredAccessTypes> modifiedAccessTypeCombinations() {
        return Stream.of(
                // creating 7 different combination (each time only 1 field is modified)
                buildRestructuredAccessTypes(false, true, "caseTypeId1", "roleName1",
                        "groupRole1", "caseGroupIdTemplate1", true),
                buildRestructuredAccessTypes(true, false, "caseTypeId1", "roleName1",
                        "groupRole1", "caseGroupIdTemplate1", true),
                buildRestructuredAccessTypes(true, true, "caseTypeId2", "roleName1",
                        "groupRole1", "caseGroupIdTemplate1", true),
                buildRestructuredAccessTypes(true, true, "caseTypeId1", "roleName2",
                        "groupRole1", "caseGroupIdTemplate1", true),
                buildRestructuredAccessTypes(true, true, "caseTypeId1", "roleName1",
                        "groupRole2", "caseGroupIdTemplate1", true),
                buildRestructuredAccessTypes(true, true, "caseTypeId1", "roleName1",
                        "groupRole1", "caseGroupIdTemplate2", true),
                buildRestructuredAccessTypes(true, true, "caseTypeId1", "roleName1",
                        "groupRole1", "caseGroupIdTemplate1", false)
        );
    }

    @Test
    void restructureCcdAccessTypesTest() {
        AccessTypesResponse accessTypesResponse = AccessTypesBuilder.buildAccessTypeResponse("accessTypesSample.json");
        RestructuredAccessTypes restructuredAccessTypes =
                AccessTypesBuilder.restructureCcdAccessTypes(accessTypesResponse);

        assertNotNull(restructuredAccessTypes);
        assertEquals(3, restructuredAccessTypes.getOrganisationProfiles().size());
        restructuredAccessTypes.getOrganisationProfiles().forEach(organisationProfile -> {
            switch (organisationProfile.getOrganisationProfileId()) {
                case "SOLICITOR_ORG" -> assertEquals(2, organisationProfile.getJurisdictions().size());
                case "DWP_GOV_ORG" -> assertEquals(2, organisationProfile.getJurisdictions().size());
                case "HMRC_GOV_ORG" -> assertEquals(1, organisationProfile.getJurisdictions().size());
            }
        });
    }

    @Test
    void identifyUpdatedOrgProfileIdsWhenEmptyInPrm() {
        AccessTypesResponse accessTypesResponse = AccessTypesBuilder.buildAccessTypeResponse("accessTypesSample.json");
        RestructuredAccessTypes restructuredAccessTypes =
                AccessTypesBuilder.restructureCcdAccessTypes(accessTypesResponse);

        RestructuredAccessTypes prmAccessTypes = RestructuredAccessTypes.builder()
                .organisationProfiles(null)
                .build();

        List<String> newOrgProfiles =
                AccessTypesBuilder.identifyUpdatedOrgProfileIds(restructuredAccessTypes, prmAccessTypes);
        List<String> expectedOrgProfiles = List.of("SOLICITOR_ORG", "DWP_GOV_ORG", "HMRC_GOV_ORG");

        assertEquals(3, newOrgProfiles.size());
        for (String newOrgProfile : newOrgProfiles) {
            assertTrue(expectedOrgProfiles.contains(newOrgProfile));
        }
    }

    @ParameterizedTest
    @MethodSource("modifiedAccessTypeCombinations")
    void identifyUpdatedOrgProfileIdsWhenOneFieldIsModified(RestructuredAccessTypes modifiedAccessTypes) {
        RestructuredAccessTypes restructuredAccessTypes =
                buildRestructuredAccessTypes(true, true, "caseTypeId1", "roleName1",
                        "groupRole1", "caseGroupIdTemplate1", true);

        List<String> modifiedOrgProfiles =
                AccessTypesBuilder.identifyUpdatedOrgProfileIds(restructuredAccessTypes, modifiedAccessTypes);

        assertTrue(modifiedOrgProfiles.contains("SOLICITOR_ORG"));
    }

    @Test
    void buildAccessTypeResponseTest() {
        AccessTypesResponse accessTypesResponse = AccessTypesBuilder.buildAccessTypeResponse("accessTypesSample.json");

        assertNotNull(accessTypesResponse);
        accessTypesResponse.getJurisdictions().forEach(jurisdiction -> {
            assertNotNull(jurisdiction.getJurisdictionId());
            assertNotNull(jurisdiction.getJurisdictionName());
            assertNotNull(jurisdiction.getAccessTypes());
        });
        assertEquals(3, accessTypesResponse.getJurisdictions().size());
    }

    @Test
    void buildAccessTypeResponseThrowsExceptionTest() {
        assertThrows(BadRequestException.class, () -> AccessTypesBuilder.buildAccessTypeResponse("invalid.json"));
    }

    private static RestructuredAccessTypes buildRestructuredAccessTypes(boolean accessMandatory,
                                                                        boolean accessDefault,
                                                                        String caseTypeId,
                                                                        String orgRoleName,
                                                                        String groupRoleName,
                                                                        String caseGroupTemplate,
                                                                        boolean groupAccessEnabled) {
        return RestructuredAccessTypes.builder()
                .organisationProfiles(List.of(
                        OrganisationProfile.builder()
                                .organisationProfileId("SOLICITOR_ORG")
                                .jurisdictions(List.of(
                                        OrganisationProfileJurisdiction.builder()
                                                .jurisdictionName("CIVIL")
                                                .accessTypes(List.of(
                                                        OrganisationProfileAccessType.builder()
                                                                .accessTypeId("accessTypeId1")
                                                                .accessMandatory(accessMandatory)
                                                                .accessDefault(accessDefault)
                                                                .roles(List.of(
                                                                        AccessTypeRole.builder()
                                                                                .caseTypeId(caseTypeId)
                                                                                .organisationalRoleName(orgRoleName)
                                                                                .groupRoleName(groupRoleName)
                                                                                .caseGroupIdTemplate(caseGroupTemplate)
                                                                                .groupAccessEnabled(groupAccessEnabled)
                                                                                .build()
                                                                )).build()
                                                )).build()
                                )).build()
                )).build();
    }
}
