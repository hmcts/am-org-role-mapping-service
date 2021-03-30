package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Setter;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
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
    public static final String PRIMARY_LOCATION_ID = "219164";

    private static String id_1 = "7c12a4bc-450e-4290-8063-b387a5d5e0b7";
    private static String id_2 = "21334a2b-79ce-44eb-9168-2d49a744be9c";

    private static final String PROCESS_ID = "staff-organisational-role-mapping";
    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";

    private UserAccessProfileBuilder() {

    }

    public static Map<String, Set<UserAccessProfile>> buildUserAccessProfiles() {

        Map<String, Set<UserAccessProfile>> userProfileMapping = new HashMap<>();

        List<UserAccessProfile> userAccessProfiles1 = Arrays.asList(buildUserAccessProfileForRoleId1(),
                buildUserAccessProfileForRoleId2());
        List<UserAccessProfile> userAccessProfiles2 = Arrays.asList(buildUserAccessProfileForRoleId3(),
                buildUserAccessProfileForRoleId4());
        userProfileMapping.put(userAccessProfiles1.get(0).getId(),
                new HashSet<>(userAccessProfiles1));
        userProfileMapping.put(userAccessProfiles2.get(0).getId(),
                new HashSet<>(userAccessProfiles1));

        return userProfileMapping;
    }

    public static UserAccessProfile buildUserAccessProfileForRoleId1() {

        return UserAccessProfile.builder()
                .id(ID1)
                .primaryLocationId(PRIMARY_LOCATION_ID)
                .serviceCode("BFA1")
                .roleId("1")
                .suspended(true)
                .build();
    }

    public static UserAccessProfile buildUserAccessProfileForRoleId2() {

        return UserAccessProfile.builder()
                .id(ID1)
                .primaryLocationId(PRIMARY_LOCATION_ID)
                .serviceCode("BFA1")
                .roleId("1")
                .suspended(true)
                .build();
    }

    public static UserAccessProfile buildUserAccessProfileForRoleId3() {

        return UserAccessProfile.builder()
                .id(ID2)
                .primaryLocationId(PRIMARY_LOCATION_ID)
                .serviceCode("BFA1")
                .roleId("1")
                .suspended(true)
                .build();
    }

    public static UserAccessProfile buildUserAccessProfileForRoleId4() {

        return UserAccessProfile.builder()
                .id(ID2)
                .primaryLocationId(PRIMARY_LOCATION_ID)
                .serviceCode("BFA1")
                .roleId("1")
                .suspended(true)
                .build();
    }

    public static UserRequest buildUserRequest() {
        return UserRequest.builder()
                .userIds(Arrays.asList(ID1, "123e4567-e89b-42d3-a456-556642445698"))
                .build();
    }

    public static UserAccessProfile buildUserAccessProfile1(boolean suspended) {
        return UserAccessProfile.builder().id(id_1).suspended(suspended).areaOfWorkId("London")
                .primaryLocationId("123456").primaryLocationName("south-east").roleId("1")
                .serviceCode("BFA1").roleName(ROLE_NAME_STCW).build();
    }

    public static UserAccessProfile buildUserAccessProfile2(boolean suspended) {
        return UserAccessProfile.builder().id(id_2).suspended(suspended).areaOfWorkId("London")
                .primaryLocationId("123457").primaryLocationName("south-east").roleId("1")
                .serviceCode("BFA1").roleName(ROLE_NAME_TCW).build();
    }

    public static Set<UserAccessProfile> buildUserAccessProfileSet(boolean suspended1, boolean suspended2) {
        Set<UserAccessProfile> userAccessProfileSet = new HashSet<>();
        userAccessProfileSet.add(buildUserAccessProfile1(suspended1));
        userAccessProfileSet.add(buildUserAccessProfile2(suspended2));
        return userAccessProfileSet;
    }

    public static Map<String, Set<UserAccessProfile>> buildUserAccessProfileMap(boolean suspended1,
                                                                                boolean suspended2) {

        HashMap<String, Set<UserAccessProfile>> userAccessProfiles = new HashMap<>();
        userAccessProfiles.put(id_1, buildUserAccessProfileSet(suspended1, suspended2));
        userAccessProfiles.put(id_2, buildUserAccessProfileSet(suspended1, suspended2));
        return userAccessProfiles;
    }


    public static List<UserProfile> buildUserProfile(UserRequest userRequest) {

        Set<UserProfile> userProfiles = new LinkedHashSet<>();


        userRequest.getUserIds().forEach(userId -> {
            try (InputStream inputStream =
                         CRDFeignClientFallback.class.getClassLoader().getResourceAsStream("userProfileSample.json")) {
                assert inputStream != null;
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategy.SnakeCaseStrategy());
                UserProfile userProfile = objectMapper.readValue(inputStream, UserProfile.class);
                userProfile.setId(userId);
                userProfiles.add(userProfile);


            } catch (Exception e) {
                throw new BadRequestException("Either the user request is not valid or sample json is missing.");
            }


        });
        return new ArrayList<>(userProfiles);
    }
}
