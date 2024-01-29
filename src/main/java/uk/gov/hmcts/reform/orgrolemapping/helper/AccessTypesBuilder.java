package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccessTypesBuilder {

    public static RestructuredAccessTypes restructureCcdAccessTypes(AccessTypesResponse accessTypesResponse) {
        Map<String, List<OrganisationProfileJurisdiction>> organisationProfilesMap = new HashMap<>();

        for (AccessTypeJurisdiction jurisdiction : accessTypesResponse.getJurisdictions()) {
            // map which associates each `organisation profile id` -> `access types` for each jurisdiction
            Map<String, List<OrganisationProfileAccessType>> organisationProfilesAccessTypesMap = new HashMap<>();

            String jurisdictionTempKey = jurisdiction.getJurisdictionName();

            for (AccessType accessType : jurisdiction.getAccessTypes()) {
                OrganisationProfileAccessType orgProfileAccessType = new OrganisationProfileAccessType(
                        accessType.getAccessTypeId(),
                        accessType.isAccessMandatory(),
                        accessType.isAccessDefault(),
                        accessType.getRoles()
                );

                String orgProfileTempKey = accessType.getOrganisationProfileId();

                if (!organisationProfilesAccessTypesMap.containsKey(orgProfileTempKey)) {
                    organisationProfilesAccessTypesMap.put(orgProfileTempKey, new ArrayList<>());
                }
                organisationProfilesAccessTypesMap.get(orgProfileTempKey).add(orgProfileAccessType);
            }

            organisationProfilesAccessTypesMap.forEach((k, v) -> {
                OrganisationProfileJurisdiction orgProfileJurisdiction = new OrganisationProfileJurisdiction();
                orgProfileJurisdiction.setJurisdictionName(jurisdictionTempKey);
                orgProfileJurisdiction.setAccessTypes(v);

                if (!organisationProfilesMap.containsKey(k)) {
                    organisationProfilesMap.put(k, new ArrayList<>());
                }
                organisationProfilesMap.get(k).add(orgProfileJurisdiction);
            });
        }

        List<OrganisationProfile> organisationProfileList = organisationProfilesMap.keySet().stream()
                .map(key -> new OrganisationProfile(key, organisationProfilesMap.get(key)))
                .toList();

        return new RestructuredAccessTypes(organisationProfileList);
    }

    public static List<String> identifyUpdatedOrgProfileIds(RestructuredAccessTypes restructuredCcdAccessTypes,
                                                            RestructuredAccessTypes prmStoredAccessTypes) {
        if (prmStoredAccessTypes.getOrganisationProfiles() == null) {
            return getOrgProfileIds(restructuredCcdAccessTypes);
        }

        List<String> modifiedOrgProfileIds = new ArrayList<>();

        // compare PRM stored access types with CCD access types for each organisationProfileId
        for (OrganisationProfile organisationProfile : prmStoredAccessTypes.getOrganisationProfiles()) {
            String currentOrgProfile = organisationProfile.getOrganisationProfileId();

            if (!getAccessTypes(prmStoredAccessTypes, currentOrgProfile).equals(
                    getAccessTypes(restructuredCcdAccessTypes, currentOrgProfile)
            )) {
                modifiedOrgProfileIds.add(currentOrgProfile);
            }
        }

        // identify new organisationProfileId
        List<String> newOrgProfileId = getOrgProfileIds(restructuredCcdAccessTypes).stream()
                .filter(el -> !getOrgProfileIds(prmStoredAccessTypes).contains(el))
                .toList();

        modifiedOrgProfileIds.addAll(newOrgProfileId);

        return modifiedOrgProfileIds;
    }

    private static List<OrganisationProfileAccessType> getAccessTypes(RestructuredAccessTypes restructuredAccessTypes,
                                                                      String orgProfile) {
        return restructuredAccessTypes.getOrganisationProfiles().stream()
                .filter(organisationProfile -> organisationProfile.getOrganisationProfileId().equals(orgProfile))
                .flatMap(organisationProfile -> organisationProfile.getJurisdictions().stream())
                .flatMap(organisationProfileJurisdiction -> organisationProfileJurisdiction.getAccessTypes().stream())
                .collect(Collectors.toList());
    }

    private static List<String> getOrgProfileIds(RestructuredAccessTypes restructuredAccessTypes) {
        return restructuredAccessTypes.getOrganisationProfiles().stream()
                .map(OrganisationProfile::getOrganisationProfileId)
                .collect(Collectors.toList());
    }

    public static AccessTypesResponse buildAccessTypeResponse(String resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(
                    new File("src/main/resources/" + resource),
                    AccessTypesResponse.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid sample json file or missing.");
        }
    }
}
