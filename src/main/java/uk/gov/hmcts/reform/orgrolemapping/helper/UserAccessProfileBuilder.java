package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Setter;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Setter
public class UserAccessProfileBuilder {
    public static final String ID1 = "123e4567-e89b-42d3-a456-556642445678";
    public static final String ID2 = "123e4567-e89b-42d3-a456-556642445699";

    private UserAccessProfileBuilder() {

    }

    public static Map<String, Set<UserAccessProfile>> buildUserAccessProfiles() {

        Map<String, Set<UserAccessProfile>> userProfileMapping = new HashMap<>();

        List<UserAccessProfile> userAccessProfiles1 = Arrays.asList(buildUserAccessProfileForRoleId1(),
                buildUserAccessProfileForRoleId2());
        List<UserAccessProfile> userAccessProfiles2 = Arrays.asList(buildUserAccessProfileForRoleId3(),
                buildUserAccessProfileForRoleId4());
        userProfileMapping.put(userAccessProfiles1.get(0).getId(),
                new HashSet<UserAccessProfile>(userAccessProfiles1));
        userProfileMapping.put(userAccessProfiles2.get(0).getId(),
                new HashSet<UserAccessProfile>(userAccessProfiles1));

        return userProfileMapping;
    }

    public static UserAccessProfile buildUserAccessProfileForRoleId1() {

        return UserAccessProfile.builder()
                .id(ID1)
                .primaryLocationId("219164")
                .serviceCode("BFA1")
                .roleId("1")
                .deleteFlag(true)
                .build();
    }

    public static UserAccessProfile buildUserAccessProfileForRoleId2() {

        return UserAccessProfile.builder()
                .id(ID1)
                .primaryLocationId("219164")
                .serviceCode("BFA1")
                .roleId("2")
                .deleteFlag(true)
                .build();
    }

    public static UserAccessProfile buildUserAccessProfileForRoleId3() {

        return UserAccessProfile.builder()
                .id(ID2)
                .primaryLocationId("219164")
                .serviceCode("BFA1")
                .roleId("1")
                .deleteFlag(true)
                .build();
    }

    public static UserAccessProfile buildUserAccessProfileForRoleId4() {

        return UserAccessProfile.builder()
                .id(ID2)
                .primaryLocationId("219164")
                .serviceCode("BFA1")
                .roleId("2")
                .deleteFlag(true)
                .build();
    }

    public static UserRequest buildUserRequest() {
        return UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445678", "123e4567-e89b-42d3-a456-556642445698"))
                .build();
    }


    public static List<UserProfile> buildUserProfile(UserRequest userRequest) {

        Set<UserProfile> userProfiles = new LinkedHashSet<>();


        userRequest.getUsers().forEach(userId -> {
            try (InputStream inputStream =
                         CRDFeignClientFallback.class.getClassLoader().getResourceAsStream("userProfileSample.json")) {
                assert inputStream != null;
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                UserProfile userProfile = objectMapper.readValue(inputStream, UserProfile.class);
                userProfile.setId(userId);
                userProfiles.add(userProfile);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        });
        return new ArrayList<>(userProfiles);
    }
}
