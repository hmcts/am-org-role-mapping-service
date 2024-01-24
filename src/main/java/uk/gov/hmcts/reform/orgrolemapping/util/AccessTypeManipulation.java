package uk.gov.hmcts.reform.orgrolemapping.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeJurisdiction;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public class AccessTypeManipulation {

    private static final Logger LOG = LoggerFactory.getLogger(AccessTypeManipulation.class);

    private List<String>  organisationProfileIdsAccessTypedefinitionsModified;

    public AccessTypeManipulation() {
    }

    public List<OrganisationProfile>  restructureToOrganisationProfiles(List<AccessTypeJurisdiction> jurisdictions)
            throws JsonProcessingException {
        List<Jurisdiction> listOfJurisdictions  = new ArrayList<>();
        List<OrganisationProfile> organisationProfiles = new ArrayList<>();

        for (AccessTypeJurisdiction accessTypeJurisdiction: jurisdictions) {
            Jurisdiction jurisdiction = new Jurisdiction();
            List<OrganisationProfileAccessType> organisationProfileAccessTypes =
                    accessTypeJurisdiction.getAccessTypes().stream()
                    .map(accessType -> new OrganisationProfileAccessType(
                            accessType.getAccessTypeId(),
                            accessType.isAccessMandatory(),
                            accessType.isAccessDefault(),
                            accessType.getRoles()))
                    .collect(Collectors.toList());

            jurisdiction.setJurisdictionName(accessTypeJurisdiction.getJurisdictionName());
            jurisdiction.setAccessTypes(organisationProfileAccessTypes);
            listOfJurisdictions.add(jurisdiction);
            for (AccessType accessType: accessTypeJurisdiction.getAccessTypes()) {
                OrganisationProfile orgProfile = new OrganisationProfile();
                orgProfile.setOrganisationProfileId(accessType.getOrganisationProfileId());
                organisationProfiles.add(orgProfile);
            }
        }

        for (OrganisationProfile orgp :organisationProfiles) {
            orgp.setJurisdictions(listOfJurisdictions);
        }
        return organisationProfiles;
    }

    public Boolean isAccessTypeSameAsOrganisationProfileAccessType(List<OrganisationProfile> savedAccessTypes,
                                                                     List<OrganisationProfile> organisationProfiles) {

        boolean matched = true;

        //performing sequential operations on data from collections, which includes filtering
        // the differences between lists
        //repeated calling of List.contains() can be a costly operation for larger lists
        List<OrganisationProfile> differences = organisationProfiles.stream()
                .filter(element -> !savedAccessTypes.contains(element))
                .collect(Collectors.toList());
        if (!differences.isEmpty()) {
            matched = false;
        }
        setOrganisationChangedProfileIDs(differences);
        return matched;
    }

    public Boolean isAccessTypeSameAsOrganisationProfileAccessTypeUsingSets(List<OrganisationProfile> localAccessTypes,
                                                                   List<OrganisationProfile> organisationProfiles) {

        boolean matched = true;
        //converting the List to a Set will have the effect of duplicating and reordering it
        List<OrganisationProfile> differences = new ArrayList<>(Sets.difference(Sets.newHashSet(organisationProfiles),
                Sets.newHashSet(localAccessTypes)));
        if (!differences.isEmpty()) {
            matched = false;
        }
        setOrganisationChangedProfileIDs(differences);
        return matched;
    }

    public List<OrganisationProfile> jsonToOrganisationProfile(String accessTypes) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<OrganisationProfile> organisationProfiles = null;
        if (accessTypes != null && !accessTypes.isEmpty()) {
            //need to check for empty object
            // as Arrays causes error where accessTypesjson = '{}'
            organisationProfiles = Arrays.asList(objectMapper.readValue(accessTypes, OrganisationProfile[].class));
        }
        return organisationProfiles;
    }

    public String organisationProfileToJsonString(List<OrganisationProfile> organisationProfiles)
            throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String accessTypes;

        accessTypes = objectMapper.writeValueAsString(organisationProfiles);

        return accessTypes;
    }

    public void setOrganisationChangedProfileIDs(List<OrganisationProfile> differences) {
        organisationProfileIdsAccessTypedefinitionsModified = new ArrayList<>();
        if (differences != null && !differences.isEmpty()) {
            for (OrganisationProfile organisationProfile : differences) {
                organisationProfileIdsAccessTypedefinitionsModified.add(organisationProfile.getOrganisationProfileId());
            }
        }

    }

}
