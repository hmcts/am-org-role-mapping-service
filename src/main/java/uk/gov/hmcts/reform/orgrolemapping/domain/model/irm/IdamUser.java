package uk.gov.hmcts.reform.orgrolemapping.domain.model.irm;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class IdamUser {

    private String id;
    private String email;
    private String forename;
    private String surname;
    private String displayName;
    private List<String> roleNames;
    private String ssoId;
    private String ssoProvider;
    private AccountStatus accountStatus;
    private RecordType recordType;
    private ZonedDateTime createDate;
    private ZonedDateTime lastModified;
    private ZonedDateTime accessLockedDate;
    private ZonedDateTime lastLoginDate;
}
