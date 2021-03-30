package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserProfilesResponse implements Serializable {
    @JsonProperty(value = "totalRecords")
    private int totalRecords;
    @JsonProperty(value = "staffProfile")
    private Collection<UserProfile> userProfiles;
}