package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    private String clientId; // this will be extracted and returned by RAS.
    private String authenticatedUserId; // this will be extracted and returned by RAS.
    private String correlationId;//need to generate a new UUID and add here.
    private String assignerId; //this needs to be set as per the static ORM account.
    private RequestType requestType; // will be configured based upon the delete flag from RD user profile
    private String process; // this will be set to staff-organisational-role-mapping for staff users
    private String reference; // the IDAM id of the actor
    private boolean replaceExisting; // set to true
}
