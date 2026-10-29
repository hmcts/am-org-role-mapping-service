package uk.gov.hmcts.reform.orgrolemapping.oidc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OIdcIdamConfiguration {

    private String userId;
    private String password;
    private String clientId;
    private String clientSecret;
    private String scope;
    private String grantType;

    @Autowired
    public OIdcIdamConfiguration(
            @Value("${idam.client.admin.userId:}") String userId,
            @Value("${idam.client.admin.secret:}") String password,
            @Value("${idam.client.irm.clientId:}") String clientId,
            @Value("${idam.client.irm.secret:}") String clientSecret
    ) {
        this.userId = userId;
        this.password = password;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = "client_credentials";
        this.scope = "view-user search-invitation revoke-invitation create-invitation";
    }

}