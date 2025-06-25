package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfessionalUser {

    // NB: corresponds to PRD: src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/
    //                                                                       OrganisationUserResponse.java

    private String userIdentifier;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime lastUpdated;
    private LocalDateTime deleted;
    private List<UserAccessType> userAccessTypes;

}
