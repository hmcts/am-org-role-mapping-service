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
            @Value("${idam.client.id:}") String clientId,
            @Value("${idam.client.secret:}") String clientSecret
    ) {
        this.userId = userId;
        this.password = password;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = "client_credentials";
        this.scope = "openid profile roles manage-user search-user create-user view-user update-user";
    }

}
