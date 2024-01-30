package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
import static org.junit.jupiter.api.Assertions.assertFalse;

class AccessTypesBuilderTest {

    private static final String CHANGED = "CHANGED";
    private static final Boolean CHANGED_BOOL = Boolean.FALSE;

    private static final OrganisationProfile SOLICITOR_ORG_PROFILE =
            buildOrganisationProfile("SOLICITOR_ORG", "CIVIL", "accessTypeId1", true, true,
                    "caseTypeId1", "roleName1", "groupRole1", "caseGroupIdTemplate1", true);

    private static final OrganisationProfile DWP_ORG_PROFILE =
            buildOrganisationProfile("DWP_GOV_ORG", "SSCS", "accessTypeId1", true, true,
                    "caseTypeId1", "roleName1", "groupRole1", "caseGroupIdTemplate1", true);

    private static final OrganisationProfile HMRC_ORG_PROFILE =
            buildOrganisationProfile("HMRC_GOV_ORG", "IA", "accessTypeId1", true, true,
                    "caseTypeId1", "roleName1", "groupRole1", "caseGroupIdTemplate1", true);

    private static final OrganisationProfile MODIFIED_SOLICITOR_ORG_PROFILE =
            buildOrganisationProfile("SOLICITOR_ORG", "CIVIL", CHANGED, true, true,
                    "caseTypeId1", "roleName1", "groupRole1", "caseGroupIdTemplate1", true);

    private static final OrganisationProfile MODIFIED_DWP_ORG_PROFILE =
            buildOrganisationProfile("DWP_GOV_ORG", "SSCS", CHANGED, true, true,
                    "caseTypeId1", "roleName1", "groupRole1", "caseGroupIdTemplate1", true);

    static Stream<Arguments> modifiedAccessTypeCombinations() {
        return Stream.of(
                // no changed so no org profiles should be identified
                Arguments.of(buildRestructuredAccessTypes(
                                List.of(SOLICITOR_ORG_PROFILE, DWP_ORG_PROFILE, HMRC_ORG_PROFILE)
                        ), false, false, false
                ),
                // only updating access types for SOLICITOR_ORG, should return only SOLICITOR_ORG
                // each variation has value CHANGE or CHANGE_BOOL to make it easier to spot the diff
                Arguments.of(buildRestructuredAccessTypes(
                                List.of(
                                        buildOrganisationProfile("SOLICITOR_ORG", "CIVIL", CHANGED, true, true,
                                                "caseTypeId1", "roleName1", "groupRole1", "caseGroupIdTemplate1",
                                                true),
                                        DWP_ORG_PROFILE, HMRC_ORG_PROFILE
                                )
                        ), true, false, false
                ),
                Arguments.of(buildRestructuredAccessTypes(
                                List.of(
                                        buildOrganisationProfile("SOLICITOR_ORG", "CIVIL", "accessTypeId1",
                                                CHANGED_BOOL, true, "caseTypeId1", "roleName1", "groupRole1",
                                                "caseGroupIdTemplate1", true),
                                        DWP_ORG_PROFILE, HMRC_ORG_PROFILE
                                )
                        ), true, false, false
                ),
                Arguments.of(buildRestructuredAccessTypes(
                                List.of(
                                        buildOrganisationProfile("SOLICITOR_ORG", "CIVIL", "accessTypeId1", true,
                                                CHANGED_BOOL, "caseTypeId1", "roleName1", "groupRole1",
                                                "caseGroupIdTemplate1", true),
                                        DWP_ORG_PROFILE, HMRC_ORG_PROFILE
                                )
                        ), true, false, false
                ),
                // modified both SOLICITOR_ORG & DWP_GOV_ORG
                Arguments.of(buildRestructuredAccessTypes(
                                List.of(
                                        MODIFIED_SOLICITOR_ORG_PROFILE,
                                        buildOrganisationProfile("DWP_GOV_ORG", "SSCS", "accessTypeId1", true, true,
                                                CHANGED, "roleName1", "groupRole1", "caseGroupIdTemplate1", true),
                                        HMRC_ORG_PROFILE
                                )
                        ), true, true, false
                ),
                Arguments.of(buildRestructuredAccessTypes(
                                List.of(
                                        MODIFIED_SOLICITOR_ORG_PROFILE,
                                        buildOrganisationProfile("DWP_GOV_ORG", "SSCS", "accessTypeId1", true, true,
                                                "caseTypeId1", CHANGED, "groupRole1", "caseGroupIdTemplate1", true),
                                        HMRC_ORG_PROFILE
                                )
                        ), true, true, false
                ),
                Arguments.of(buildRestructuredAccessTypes(
                                List.of(
                                        MODIFIED_SOLICITOR_ORG_PROFILE,
                                        buildOrganisationProfile("DWP_GOV_ORG", "SSCS", "accessTypeId1", true, true,
                                                "caseTypeId1", "roleName1", CHANGED, "caseGroupIdTemplate1", true),
                                        HMRC_ORG_PROFILE
                                )
                        ), true, true, false
                ),
                // modified SOLICITOR_ORG, DWP_GOV_ORG & HMRC_ORG
                Arguments.of(buildRestructuredAccessTypes(
                                List.of(
                                        MODIFIED_SOLICITOR_ORG_PROFILE,
                                        MODIFIED_DWP_ORG_PROFILE,
                                        buildOrganisationProfile("HMRC_GOV_ORG", "IA", "accessTypeId1", true, true,
                                                "caseTypeId1", "roleName1", "groupRole1", CHANGED, true)
                                )
                        ), true, true, true
                ),
                Arguments.of(buildRestructuredAccessTypes(
                                List.of(
                                        MODIFIED_SOLICITOR_ORG_PROFILE,
                                        MODIFIED_DWP_ORG_PROFILE,
                                        MODIFIED_DWP_ORG_PROFILE,
                                        buildOrganisationProfile("HMRC_GOV_ORG", "IA", "accessTypeId1", true, true,
                                                "caseTypeId1", "roleName1", "groupRole1", "caseGroupIdTemplate1",
                                                CHANGED_BOOL)
                                )
                        ), true, true, true
                )
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
    void identifyUpdatedOrgProfileIdsWhenEmptyInPrmTest() {
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

    @ParameterizedTest
    @MethodSource("modifiedAccessTypeCombinations")
    void identifyUpdatedOrgProfileIdsWhenOneFieldIsModifiedTest(RestructuredAccessTypes modifiedAccessTypes,
                                                                boolean updatedSolicitorOrg,
                                                                boolean updatedDwpOrg,
                                                                boolean updatedHmrcOrg) {
        RestructuredAccessTypes restructuredAccessTypes = buildRestructuredAccessTypes(List.of(
                SOLICITOR_ORG_PROFILE, DWP_ORG_PROFILE, HMRC_ORG_PROFILE
        ));

        List<String> modifiedOrgProfiles =
                AccessTypesBuilder.identifyUpdatedOrgProfileIds(restructuredAccessTypes, modifiedAccessTypes);

        if (updatedSolicitorOrg) {
            assertTrue(modifiedOrgProfiles.contains("SOLICITOR_ORG"));
        } else {
            assertFalse(modifiedOrgProfiles.contains("SOLICITOR_ORG"));
        }

        if (updatedDwpOrg) {
            assertTrue(modifiedOrgProfiles.contains("DWP_GOV_ORG"));
        } else {
            assertFalse(modifiedOrgProfiles.contains("DWP_GOV_ORG"));
        }

        if (updatedHmrcOrg) {
            assertTrue(modifiedOrgProfiles.contains("HMRC_GOV_ORG"));
        } else {
            assertFalse(modifiedOrgProfiles.contains("HMRC_GOV_ORG"));
        }
    }

    private static OrganisationProfile buildOrganisationProfile(String organisationProfileId,
                                                                String jurisdictionName,
                                                                String accessTypeId,
                                                                boolean accessMandatory,
                                                                boolean accessDefault,
                                                                String caseTypeId,
                                                                String orgRoleName,
                                                                String groupRoleName,
                                                                String caseGroupTemplate,
                                                                boolean groupAccessEnabled) {
        return OrganisationProfile.builder()
                .organisationProfileId(organisationProfileId)
                .jurisdictions(List.of(
                        OrganisationProfileJurisdiction.builder()
                                .jurisdictionName(jurisdictionName)
                                .accessTypes(List.of(
                                        OrganisationProfileAccessType.builder()
                                                .accessTypeId(accessTypeId)
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
                )).build();
    }

    private static OrganisationProfile buildOrganisationProfile(String organisationProfileId,
                                                                String jurisdictionName,
                                                                List<OrganisationProfileAccessType> accessTypes) {
        return OrganisationProfile.builder()
                .organisationProfileId(organisationProfileId)
                .jurisdictions(List.of(
                        OrganisationProfileJurisdiction.builder()
                                .jurisdictionName(jurisdictionName)
                                .accessTypes(accessTypes)
                                .build()
                )).build();
    }

    private static RestructuredAccessTypes buildRestructuredAccessTypes(List<OrganisationProfile> orgProfiles) {
        return RestructuredAccessTypes.builder()
                .organisationProfiles(orgProfiles)
                .build();
    }

    private static OrganisationProfileAccessType buildOrganisationProfileAccessType(String accessTypeId,
                                                                                    boolean accessMandatory,
                                                                                    boolean accessDefault,
                                                                                    String caseTypeId,
                                                                                    String orgRoleName,
                                                                                    String groupRoleName,
                                                                                    String caseGroupTemplate,
                                                                                    boolean groupAccessEnabled) {
        return OrganisationProfileAccessType.builder()
                .accessTypeId(accessTypeId)
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
                )).build();
    }
}
