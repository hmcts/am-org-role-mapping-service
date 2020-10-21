package uk.gov.hmcts.reform.orgrolemapping.helper;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Setter;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Setter
public class TestDataBuilder {

    private TestDataBuilder() {
    }

    public static UserInfo buildUserInfo(String uuid) {
        List<String> list = new ArrayList<>();
        return UserInfo.builder().sub("sub").uid(uuid)
                .name("James").givenName("007").familyName("Bond").roles(list).build();
    }

}