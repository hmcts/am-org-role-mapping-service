package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.crd.JobTitle;


//b. Create a new model class UserAccessProfile(id, roleId, roleName, primaryLocationId,
// primaryLocationName, areaOfWorkId, serviceCode, suspended) (which will flatten the User Profile into multiple
// userAccessProfile instances based upon roleId X serviceCode).


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseWorkerAccessProfile implements Serializable, UserAccessProfile {
    private String id;
    private String roleId;
    private String roleName;
    private String primaryLocationId;
    private String primaryLocationName;
    private String areaOfWorkId;
    private String serviceCode;
    private boolean suspended;
    private String caseAllocatorFlag;
    private String taskSupervisorFlag;
    private String regionId;
    private String staffAdmin;
    private List<String> skillCodes;

    private static final Pattern CONTESTED_PATTERN =
            Pattern.compile("^(SKILL:ABA2:).*(Contested)$");
    private static final Pattern CONSENTED_PATTERN =
            Pattern.compile("^(SKILL:ABA2:).*(?<!Contested)$");

    @JsonIgnore
    public boolean hasValidJobTitle(JobTitle... jobTitles) {
        return Arrays.stream(jobTitles)
            .anyMatch(jobTitle -> jobTitle.getRoleId().equals(roleId));
    }

    @JsonIgnore
    public boolean hasValidServiceCode(Jurisdiction jurisdiction) {
        return jurisdiction.getServiceCodes().stream()
            .anyMatch(testServiceCode -> testServiceCode.equals(serviceCode));
    }

    @JsonIgnore
    public boolean isCaseAllocator() {
        return this.caseAllocatorFlag != null && this.caseAllocatorFlag.equals("Y");
    }

    @JsonIgnore
    public boolean isTaskSupervisor() {
        return this.taskSupervisorFlag != null && this.taskSupervisorFlag.equals("Y");
    }

    @JsonIgnore
    public boolean isContestedSkill() {
        if (skillCodes == null || skillCodes.isEmpty()) {
            return false;
        }
        return skillCodes.stream()
                .anyMatch(skill -> CONTESTED_PATTERN.matcher(skill).matches());
    }

    @JsonIgnore
    public boolean isConsentedSkill() {
        if (skillCodes == null || skillCodes.isEmpty()) {
            return false;
        }
        return skillCodes.stream()
                .anyMatch(skill -> CONSENTED_PATTERN.matcher(skill).matches());
    }

}
