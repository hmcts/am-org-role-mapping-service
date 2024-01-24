package uk.gov.hmcts.reform.orgrolemapping.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.util.AccessTypeManipulation;
import uk.gov.hmcts.reform.orgrolemapping.util.TypeOfCompare;

import java.io.IOException;
import java.util.List;

public class OrganisationProfileAccessTypes {

    private final AccessTypesRepository accessTypesRepository;

    private final AccessTypeManipulation accessTypeManipulation;

    private List<String>  organisationProfileIdsAccessTypedefinitionsModified = null;

    private TypeOfCompare typeOfCompare = TypeOfCompare.STREAM; //Default

    public OrganisationProfileAccessTypes(AccessTypesRepository accessTypesRepository,
                                          AccessTypesResponse accessTypesResponse) {
        this.accessTypesRepository = accessTypesRepository;
        this.accessTypeManipulation = new AccessTypeManipulation();

        processSavedAndReceivedAccessTypes(accessTypesResponse);
    }

    public OrganisationProfileAccessTypes(AccessTypesRepository accessTypesRepository,
                                          AccessTypesResponse accessTypesResponse, TypeOfCompare typeOfCompare) {
        this.accessTypesRepository = accessTypesRepository;
        this.accessTypeManipulation = new AccessTypeManipulation();
        this.typeOfCompare = typeOfCompare;

        processSavedAndReceivedAccessTypes(accessTypesResponse);
    }

    private void processSavedAndReceivedAccessTypes(AccessTypesResponse accessTypesResponse) {
        String jsonString = getOrganisationProfilesJsonString(accessTypesResponse);
        if (jsonString != null) {
            //save the string to DB
            Long nextVal = this.accessTypesRepository.getNextAccessTypeVersion();
            AccessTypesEntity changeAccessTypesEntity = this.accessTypesRepository.findAll().stream().findFirst()
                    .orElse(null);
            changeAccessTypesEntity.setAccessTypes(jsonString);
            changeAccessTypesEntity.setVersion(nextVal);
            this.accessTypesRepository.deleteAll();
            this.accessTypesRepository.saveAndFlush(changeAccessTypesEntity);
        }
    }

    private String getOrganisationProfilesJsonString(AccessTypesResponse accessTypesResponse) {

        List<OrganisationProfile> organisationProfiles = null;
        boolean matched = false;
        List<OrganisationProfile> savedOrganisationProfiles = null;
        try {
            AccessTypesEntity savedAccessTypesEntity = accessTypesRepository.findAll().stream().findFirst()
                    .orElse(null);
            String savedAccessTypes = savedAccessTypesEntity.getAccessTypes();
            savedOrganisationProfiles = accessTypeManipulation.jsonToOrganisationProfile(savedAccessTypes);
        } catch (JsonProcessingException e) {
            if (e instanceof MismatchedInputException) {
                //not matched as first time
                matched = false;
            } else {
                throw new RuntimeException(e);
            }
        }

        // restructure the AccessTypesResponse
        try {
            organisationProfiles = accessTypeManipulation
                    .restructureToOrganisationProfiles(accessTypesResponse.getJurisdictions());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String jsonString = null;
        //Does re-structured  organisationProfiles  match saved version.
        if (savedOrganisationProfiles != null && !savedOrganisationProfiles.isEmpty()) {
            //savedOrganisationProfiles and organisationProfiles same or has been modified
            if (typeOfCompare == TypeOfCompare.STREAM) {
                matched = accessTypeManipulation
                        .isAccessTypeSameAsOrganisationProfileAccessType(savedOrganisationProfiles,
                                organisationProfiles);
            } else {
                matched = accessTypeManipulation
                        .isAccessTypeSameAsOrganisationProfileAccessTypeUsingSets(savedOrganisationProfiles,
                                organisationProfiles);

            }
        } else {
            if (organisationProfiles != null && !organisationProfiles.isEmpty()) {
                accessTypeManipulation.setOrganisationChangedProfileIDs(organisationProfiles);
            }
        }

        if (!matched) {
            try {
                jsonString = accessTypeManipulation.organisationProfileToJsonString(organisationProfiles);
                organisationProfileIdsAccessTypedefinitionsModified = accessTypeManipulation
                        .getOrganisationProfileIdsAccessTypedefinitionsModified();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonString;
    }

    public List<String> getOrganisationProfileIdsAccessTypedefinitionsModified() {
        return organisationProfileIdsAccessTypedefinitionsModified;
    }

}
