package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.ORGANISATIONS_BY_PROFILE_IDS_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.RETRIEVE_ORGANISATIONS_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.RETRIEVE_USERS_SAMPLE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.PRDFallbackResponseBuilder.USERS_BY_ORGANISATION_SAMPLE;

class PRDFallbackResponseBuilderTest {

    private static final String INVALID_JSON = "invalid.json";

    @Test
    void buildOrganisationByProfileIdsResponseTest() {
        OrganisationByProfileIdsResponse organisationByProfileIdsResponse = PRDFallbackResponseBuilder
            .buildOrganisationByProfileIdsResponse(ORGANISATIONS_BY_PROFILE_IDS_SAMPLE);

        assertNotNull(organisationByProfileIdsResponse);
        assertEquals(2, organisationByProfileIdsResponse.getOrganisationInfo().size());
        assertEquals("1", organisationByProfileIdsResponse.getOrganisationInfo().get(0).getOrganisationIdentifier());
        assertEquals("2", organisationByProfileIdsResponse.getOrganisationInfo().get(1).getOrganisationIdentifier());

        assertNotNull(organisationByProfileIdsResponse.getLastRecordInPage());
        assertNotNull(organisationByProfileIdsResponse.getMoreAvailable());
    }

    @Test
    void buildOrganisationByProfileIdsResponseThrowsExceptionTest() {
        assertThrows(BadRequestException.class, () ->
            PRDFallbackResponseBuilder.buildOrganisationByProfileIdsResponse(INVALID_JSON)
        );
    }

    @Test
    void buildOrganisationsResponseTest() {
        OrganisationsResponse organisationsResponse = PRDFallbackResponseBuilder
                .buildOrganisationsResponse(RETRIEVE_ORGANISATIONS_SAMPLE);

        assertNotNull(organisationsResponse);
        assertEquals(2, organisationsResponse.getOrganisations().size());
        assertEquals("1", organisationsResponse.getOrganisations().get(0).getOrganisationIdentifier());
        assertEquals("2", organisationsResponse.getOrganisations().get(1).getOrganisationIdentifier());
    }

    @Test
    void buildOrganisationsResponseThrowsExceptionTest() {
        assertThrows(BadRequestException.class, () ->
            PRDFallbackResponseBuilder.buildOrganisationsResponse(INVALID_JSON)
        );
    }

    @Test
    void buildRefreshUserResponseTest() {
        GetRefreshUserResponse refreshUserResponse = PRDFallbackResponseBuilder
            .buildRefreshUserResponse(RETRIEVE_USERS_SAMPLE);

        assertNotNull(refreshUserResponse);
        refreshUserResponse.getUsers().forEach(user -> {
            assertNotNull(user.getUserIdentifier());
            assertNotNull(user.getLastUpdated());
            assertNotNull(user.getOrganisationInfo());
            assertFalse(CollectionUtils.isEmpty(user.getUserAccessTypes()));
        });
        assertNotNull(refreshUserResponse.getLastRecordInPage());
        assertFalse(refreshUserResponse.isMoreAvailable());
    }

    @Test
    void buildRefreshUserResponseThrowsExceptionTest() {
        assertThrows(BadRequestException.class, () ->
            PRDFallbackResponseBuilder.buildRefreshUserResponse(INVALID_JSON)
        );
    }

    @Test
    void buildUsersByOrganisationResponseTest() {
        UsersByOrganisationResponse usersByOrganisationResponse = PRDFallbackResponseBuilder
            .buildUsersByOrganisationResponse(USERS_BY_ORGANISATION_SAMPLE);

        assertNotNull(usersByOrganisationResponse);
        usersByOrganisationResponse.getOrganisationInfo().forEach(organisation -> {
            assertNotNull(organisation.getOrganisationIdentifier());
            assertNotNull(organisation.getStatus());
            assertFalse(CollectionUtils.isEmpty(organisation.getOrganisationProfileIds()));
            organisation.getUsers().forEach(user -> {
                assertNotNull(user.getUserIdentifier());
                assertNotNull(user.getLastUpdated());
                assertFalse(CollectionUtils.isEmpty(user.getUserAccessTypes()));
            });
        });
        assertNotNull(usersByOrganisationResponse.getLastUserInPage());
        assertNotNull(usersByOrganisationResponse.getMoreAvailable());
    }

    @Test
    void buildUsersByOrganisationResponseThrowsExceptionTest() {
        assertThrows(BadRequestException.class, () ->
            PRDFallbackResponseBuilder.buildUsersByOrganisationResponse(INVALID_JSON)
        );
    }

}
