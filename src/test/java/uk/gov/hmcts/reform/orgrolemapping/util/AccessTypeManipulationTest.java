package uk.gov.hmcts.reform.orgrolemapping.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildAccessTypesResponse;

@RunWith(MockitoJUnitRunner.class)
class AccessTypeManipulationTest {


    @Test
    void comparingSavedWithRecievedAccessTypes()  {
        AccessTypeManipulation accessTypeManipulation = new AccessTypeManipulation();
        AccessTypesResponse accessTypesResponse;
        List<OrganisationProfile> organisationProfiles;
        boolean matched = false;

        try {
            accessTypesResponse = buildAccessTypesResponse();

            organisationProfiles = accessTypeManipulation
                    .restructureToOrganisationProfiles(accessTypesResponse.getJurisdictions());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String savedAccessTypes;
        try {
            savedAccessTypes = accessTypeManipulation.organisationProfileToJsonString(organisationProfiles);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        List<OrganisationProfile> savedOrganisationProfiles = null;
        try {
            savedOrganisationProfiles = accessTypeManipulation
                    .jsonToOrganisationProfile(savedAccessTypes);
        } catch (JsonProcessingException e) {
            if (e instanceof MismatchedInputException) {
                //not matched as first time
                matched = false;
            } else {
                throw new RuntimeException(e);
            }
        }
        if (savedOrganisationProfiles != null && !savedOrganisationProfiles.isEmpty()) {
            matched = accessTypeManipulation
                    .isAccessTypeSameAsOrganisationProfileAccessType(savedOrganisationProfiles, organisationProfiles);
        }

        assertTrue(matched);
    }

    @Test
    void comparingSavedWithChangedRecievedAccessTypes() throws IOException {
        AccessTypeManipulation accessTypeManipulation = new AccessTypeManipulation();
        AccessTypesResponse accessTypesResponse;
        List<OrganisationProfile> organisationProfiles;

        accessTypesResponse = buildAccessTypesResponse();

        organisationProfiles = accessTypeManipulation
                    .restructureToOrganisationProfiles(accessTypesResponse.getJurisdictions());
        String savedAccessTypes;
        try {
            savedAccessTypes = accessTypeManipulation.organisationProfileToJsonString(organisationProfiles);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<OrganisationProfile> savedOrganisationProfiles = null;
        boolean matched = false;
        try {
            savedOrganisationProfiles = accessTypeManipulation
                    .jsonToOrganisationProfile(savedAccessTypes);
        } catch (JsonProcessingException e) {
            if (e instanceof MismatchedInputException) {
                //not matched as first time
                matched = false;
            } else {
                throw new RuntimeException(e);
            }
        }

        String actualOrgProfID = organisationProfiles.get(0).getOrganisationProfileId();
        organisationProfiles.get(0).setOrganisationProfileId("NEW_PROFILEID");
        if (savedOrganisationProfiles != null && !savedOrganisationProfiles.isEmpty()) {
            matched = accessTypeManipulation
                    .isAccessTypeSameAsOrganisationProfileAccessType(savedOrganisationProfiles, organisationProfiles);
        }


        assertFalse(matched);

    }

    @Test
    void comparingSavedWithChangedRecievedAccessTypesUsingSet()  {
        AccessTypeManipulation accessTypeManipulation = new AccessTypeManipulation();
        AccessTypesResponse accessTypesResponse;
        List<OrganisationProfile> organisationProfiles;

        try {
            accessTypesResponse = buildAccessTypesResponse();

            organisationProfiles = accessTypeManipulation
                    .restructureToOrganisationProfiles(accessTypesResponse.getJurisdictions());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String savedAccessTypes;
        try {
            savedAccessTypes = accessTypeManipulation.organisationProfileToJsonString(organisationProfiles);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        boolean matched = false;
        List<OrganisationProfile> savedOrganisationProfiles = null;
        try {
            savedOrganisationProfiles = accessTypeManipulation
                    .jsonToOrganisationProfile(savedAccessTypes);
        } catch (JsonProcessingException e) {
            if (e instanceof MismatchedInputException) {
                //not matched as first time
                matched = false;
            } else {
                throw new RuntimeException(e);
            }
        }

        String actualOrgProfID = organisationProfiles.get(0).getOrganisationProfileId();
        organisationProfiles.get(0).setOrganisationProfileId("NEW_PROFILEID");
        if (savedOrganisationProfiles != null && !savedOrganisationProfiles.isEmpty()) {
            matched = accessTypeManipulation
                    .isAccessTypeSameAsOrganisationProfileAccessTypeUsingSets(savedOrganisationProfiles,
                            organisationProfiles);
        }

        List<String> changedOrgIds = accessTypeManipulation.getOrganisationProfileIdsAccessTypedefinitionsModified();

        assertFalse(matched);
        assertEquals("NEW_PROFILEID", changedOrgIds.get(0));

    }



    @Test
    void mismatchedExceptionWhenComparingSavedEmptyWithRecievedAccessTypes() throws IOException {
        AccessTypeManipulation accessTypeManipulation = new AccessTypeManipulation();
        AccessTypesResponse accessTypesResponse;
        List<OrganisationProfile> organisationProfiles;
        boolean matched = false;

        // uses the "src/main/resources/accessTypesSample.json" received from GA-20
        accessTypesResponse = buildAccessTypesResponse();

        organisationProfiles = accessTypeManipulation
                    .restructureToOrganisationProfiles(accessTypesResponse.getJurisdictions());

        // Empty Saved access_types
        // getting it ito an Object
        String savedAccessTypes = "{}";
        List<OrganisationProfile> savedOrganisationProfiles = null;
        try {
            savedOrganisationProfiles = accessTypeManipulation
                    .jsonToOrganisationProfile(savedAccessTypes);
        } catch (JsonProcessingException e) {
            if (e instanceof MismatchedInputException) {
                //not matched as first time
                matched = false;
            } else {
                throw new RuntimeException(e);
            }
        }
        //Checking if it matches. Off course it does not match
        if (savedOrganisationProfiles != null && !savedOrganisationProfiles.isEmpty()) {
            matched = accessTypeManipulation
                    .isAccessTypeSameAsOrganisationProfileAccessType(savedOrganisationProfiles, organisationProfiles);
        }

        //should save organisationProfiles
        try {
            String jsonString = accessTypeManipulation.organisationProfileToJsonString(organisationProfiles);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        assertFalse(matched);

    }
}
