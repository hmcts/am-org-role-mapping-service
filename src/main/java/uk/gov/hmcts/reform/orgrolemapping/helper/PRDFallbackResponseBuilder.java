package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;

import java.io.File;

public class PRDFallbackResponseBuilder {

    public static final String ORGANISATIONS_BY_PROFILE_IDS_SAMPLE = "organisationsByProfileIdsSample.json";
    public static final String RETRIEVE_ORGANISATIONS_SAMPLE = "retrieveOrganisationsSample.json";
    public static final String RETRIEVE_USERS_SAMPLE = "retrieveUsersSample.json";
    public static final String USERS_BY_ORGANISATION_SAMPLE = "usersByOrganisationSample.json";

    private static final String RESOURCES_PATH = "src/main/resources/samples/prd/";

    private PRDFallbackResponseBuilder() {
        // Hide Utility Class Constructor : Utility classes should not have a public or
        // default constructor (squid:S1118)
    }

    public static OrganisationByProfileIdsResponse buildOrganisationByProfileIdsResponse(String resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(
                    new File(RESOURCES_PATH + resource),
                    OrganisationByProfileIdsResponse.class);
        } catch (Exception e) {
            throw new BadRequestException(
                "Invalid sample json file or missing for buildOrganisationByProfileIdsResponse."
            );
        }
    }

    public static OrganisationsResponse buildOrganisationsResponse(String resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(
                    new File(RESOURCES_PATH + resource),
                    OrganisationsResponse.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid sample json file or missing for buildOrganisationsResponse.");
        }
    }

    public static GetRefreshUserResponse buildRefreshUserResponse(String resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(
                new File(RESOURCES_PATH + resource),
                GetRefreshUserResponse.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid sample json file or missing for buildRefreshUserResponse.");
        }
    }

    public static UsersByOrganisationResponse buildUsersByOrganisationResponse(String resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(
                new File(RESOURCES_PATH + resource),
                UsersByOrganisationResponse.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid sample json file or missing for buildUsersByOrganisationResponse.");
        }
    }

}
