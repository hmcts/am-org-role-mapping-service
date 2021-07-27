package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import lombok.Setter;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

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

    private UserAccessProfileBuilder() {

    }

    public static Map<String, Set<CaseWorkerAccessProfile>> buildUserAccessProfiles() {

        Map<String, Set<CaseWorkerAccessProfile>> userProfileMapping = new HashMap<>();

        List<CaseWorkerAccessProfile> caseWorkerAccessProfiles1 = Arrays.asList(buildUserAccessProfileForRoleId1(),
                buildUserAccessProfileForRoleId2());
        List<CaseWorkerAccessProfile> caseWorkerAccessProfiles2 = Arrays.asList(buildUserAccessProfileForRoleId3(),
                buildUserAccessProfileForRoleId4());
        userProfileMapping.put(caseWorkerAccessProfiles1.get(0).getId(),
                new HashSet<>(caseWorkerAccessProfiles1));
        userProfileMapping.put(caseWorkerAccessProfiles2.get(0).getId(),
                new HashSet<>(caseWorkerAccessProfiles1));

        return userProfileMapping;
    }

    public static CaseWorkerAccessProfile buildUserAccessProfileForRoleId1() {

        return CaseWorkerAccessProfile.builder()
                .id(ID1)
                .primaryLocationId(PRIMARY_LOCATION_ID)
                .serviceCode("BFA1")
                .roleId("1")
                .suspended(true)
                .build();
    }

    public static CaseWorkerAccessProfile buildUserAccessProfileForRoleId2() {

        return CaseWorkerAccessProfile.builder()
                .id(ID1)
                .primaryLocationId(PRIMARY_LOCATION_ID)
                .serviceCode("BFA1")
                .roleId("2")
                .suspended(true)
                .build();
    }

    public static CaseWorkerAccessProfile buildUserAccessProfileForRoleId3() {

        return CaseWorkerAccessProfile.builder()
                .id(ID2)
                .primaryLocationId(PRIMARY_LOCATION_ID)
                .serviceCode("BFA1")
                .roleId("1")
                .suspended(true)
                .build();
    }

    public static CaseWorkerAccessProfile buildUserAccessProfileForRoleId4() {

        return CaseWorkerAccessProfile.builder()
                .id(ID2)
                .primaryLocationId(PRIMARY_LOCATION_ID)
                .serviceCode("BFA1")
                .roleId("2")
                .suspended(true)
                .build();
    }

    public static UserRequest buildUserRequest() {
        return UserRequest.builder()
                .userIds(Arrays.asList(ID1, "123e4567-e89b-42d3-a456-556642445698"))
                .build();
    }


    public static List<CaseWorkerProfile> buildUserProfile(UserRequest userRequest, String resource) {

        Set<CaseWorkerProfile> caseWorkerProfiles = new LinkedHashSet<>();


        userRequest.getUserIds().forEach(userId -> {
            try (InputStream inputStream =
                         UserAccessProfileBuilder.class.getClassLoader()
                                 .getResourceAsStream(resource)) {
                assert inputStream != null;
                ObjectMapper objectMapper = getObjectMapper();
                CaseWorkerProfile caseWorkerProfile = objectMapper.readValue(inputStream, CaseWorkerProfile.class);
                caseWorkerProfile.setId(userId);
                caseWorkerProfiles.add(caseWorkerProfile);


            } catch (Exception e) {
                throw new BadRequestException("Either the user request is not valid or sample json is missing.");
            }


        });
        return new ArrayList<>(caseWorkerProfiles);
    }

    public static List<CaseWorkerProfile> buildUserAccessProfile(UserRequest userRequest, String resource) {

        Set<CaseWorkerProfile> caseWorkerProfiles = new LinkedHashSet<>();


        userRequest.getUserIds().forEach(userId -> {
            try (InputStream inputStream =
                         UserAccessProfileBuilder.class.getClassLoader()
                                 .getResourceAsStream(resource)) {
                assert inputStream != null;
                ObjectMapper objectMapper = getObjectMapper();
                CaseWorkerProfile caseWorkerProfile = objectMapper.readValue(inputStream, CaseWorkerProfile.class);
                caseWorkerProfile.setId(userId);
                caseWorkerProfiles.add(caseWorkerProfile);


            } catch (Exception e) {
                throw new BadRequestException("Either the user request is not valid or sample json is missing.");
            }


        });
        return new ArrayList<>(caseWorkerProfiles);
    }

    public static List<JudicialProfile> buildJudicialProfile(UserRequest userRequest, String resource) {

        Set<JudicialProfile> judicialProfilesProfiles = new LinkedHashSet<>();


        userRequest.getUserIds().forEach(userId -> {
            try (InputStream inputStream =
                         UserAccessProfileBuilder.class.getClassLoader()
                                 .getResourceAsStream(resource)) {
                assert inputStream != null;
                ObjectMapper objectMapper = getObjectMapper();
                JudicialProfile judicialProfile = objectMapper.readValue(inputStream, JudicialProfile.class);
                judicialProfile.setElinkId(userId);
                judicialProfilesProfiles.add(judicialProfile);


            } catch (Exception e) {
                throw new BadRequestException("Either the user request is not valid or sample json is missing.");
            }


        });
        return new ArrayList<>(judicialProfilesProfiles);
    }

    @NotNull
    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategy.SnakeCaseStrategy());
        return objectMapper;
    }
}
