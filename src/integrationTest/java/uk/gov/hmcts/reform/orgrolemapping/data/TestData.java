package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;

import java.util.List;

public class TestData {

    public static String SSCS_JURISDICTION = "SSCS";
    public static String CIVIL_JURISDICTION = "CIVIL";

    public static ResponseEntity<AccessTypesResponse> setupTestData(String jurisdictionId) {

        AccessTypeRole ccdRoles = buildAccessTypeRole(jurisdictionId + "_Case_TYPE",
                jurisdictionId + "_Org_Role1", jurisdictionId + "_Group_Role1",
                jurisdictionId + "_CaseType:[GrpRoleName1]:$ORGID$", false);

        AccessType ccdAccessType = buildAccessType(jurisdictionId + "_SOLICITOR_PROFILE",
                jurisdictionId + "_ACCESS_TYPE_ID", true, true,
                true, "description for" + jurisdictionId, "hint  for" + jurisdictionId,
                3, List.of(ccdRoles));

        AccessTypeJurisdiction jurisdictions = buildJurisdictions(jurisdictionId,
                jurisdictionId + "_NAME", List.of(ccdAccessType));

        AccessTypesResponse response = buildAccessTypesResponse(List.of(jurisdictions));

        ResponseEntity<AccessTypesResponse> ccdDefinitions = new ResponseEntity<>(response, HttpStatus.OK);

        return ccdDefinitions;

    }

    public static AccessTypesResponse buildAccessTypesResponse(List<AccessTypeJurisdiction> jurisdictions) {

        return AccessTypesResponse
                .builder()
                .jurisdictions(jurisdictions)
                .build();

    }

    public static AccessTypeJurisdiction buildJurisdictions(String jurisdictionId,
                                                            String jurisdictionName, List<AccessType> accessTypes) {

        return AccessTypeJurisdiction
                .builder()
                .jurisdictionId(jurisdictionId)
                .jurisdictionName(jurisdictionName)
                .accessTypes(accessTypes)
                .build();

    }

    public static AccessType buildAccessType(String organisationProfileId, String accessTypeId,
                                             boolean accessMandatory, boolean accessDefault,
                                             boolean display, String description, String hint,
                                             Integer displayOrder, List<AccessTypeRole> roles) {

        return AccessType
                .builder()
                .organisationProfileId(organisationProfileId)
                .accessTypeId(accessTypeId)
                .accessMandatory(accessMandatory)
                .accessDefault(accessDefault)
                .display(display)
                .description(description)
                .hint(hint)
                .displayOrder(displayOrder)
                .roles(roles).build();

    }

    private static AccessTypeRole buildAccessTypeRole(String caseTypeId, String organisationalRoleName,
                                                      String groupRoleName, String caseGroupIdTemplate,
                                                      boolean groupAccessEnabled) {

        return AccessTypeRole
                .builder()
                .caseTypeId(caseTypeId)
                .organisationalRoleName(organisationalRoleName)
                .groupRoleName(groupRoleName)
                .caseGroupIdTemplate(caseGroupIdTemplate)
                .groupAccessEnabled(groupAccessEnabled)
                .build();

    }

}
