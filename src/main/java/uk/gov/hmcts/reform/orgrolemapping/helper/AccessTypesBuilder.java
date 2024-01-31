package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AccessTypesBuilder {

    public static RestructuredAccessTypes restructureCcdAccessTypes(AccessTypesResponse accessTypesResponse) {
        Map<String, Set<OrganisationProfileJurisdiction>> organisationProfilesMap = new HashMap<>();

        for (AccessTypeJurisdiction jurisdiction : accessTypesResponse.getJurisdictions()) {
            // map which associates each `organisation profile id` -> `access types` for each jurisdiction
            Map<String, Set<OrganisationProfileAccessType>> organisationProfilesAccessTypesMap = new HashMap<>();

            String jurisdictionTempKey = jurisdiction.getJurisdictionId();

            for (AccessType accessType : jurisdiction.getAccessTypes()) {
                OrganisationProfileAccessType orgProfileAccessType = new OrganisationProfileAccessType(
                        accessType.getAccessTypeId(),
                        accessType.isAccessMandatory(),
                        accessType.isAccessDefault(),
                        new HashSet<>(accessType.getRoles())
                );

                String orgProfileTempKey = accessType.getOrganisationProfileId();

                if (!organisationProfilesAccessTypesMap.containsKey(orgProfileTempKey)) {
                    organisationProfilesAccessTypesMap.put(orgProfileTempKey, new HashSet<>());
                }
                organisationProfilesAccessTypesMap.get(orgProfileTempKey).add(orgProfileAccessType);
            }

            organisationProfilesAccessTypesMap.forEach((k, v) -> {
                OrganisationProfileJurisdiction orgProfileJurisdiction = new OrganisationProfileJurisdiction();
                orgProfileJurisdiction.setJurisdictionId(jurisdictionTempKey);
                orgProfileJurisdiction.setAccessTypes(v);

                if (!organisationProfilesMap.containsKey(k)) {
                    organisationProfilesMap.put(k, new HashSet<>());
                }
                organisationProfilesMap.get(k).add(orgProfileJurisdiction);
            });
        }

        Set<OrganisationProfile> organisationProfileList = organisationProfilesMap.keySet().stream()
                .map(key -> new OrganisationProfile(key, organisationProfilesMap.get(key)))
                .collect(Collectors.toSet());

        return new RestructuredAccessTypes(organisationProfileList);
    }

    public static List<String> identifyUpdatedOrgProfileIds(RestructuredAccessTypes restructuredCcdAccessTypes,
                                                            RestructuredAccessTypes prmStoredAccessTypes) {
        if (prmStoredAccessTypes.getOrganisationProfiles() == null) {
            log.info("no organisation profile/s in PRM database, hence returning all from CCD");
            return getOrgProfileIds(restructuredCcdAccessTypes);
        }

        List<String> modifiedOrgProfileIds = new ArrayList<>();

        // compare each PRM stored `org profile` with CCD representation of `org profile`
        for (OrganisationProfile organisationProfile : prmStoredAccessTypes.getOrganisationProfiles()) {
            String currentOrgProfile = organisationProfile.getOrganisationProfileId();

            if (!getOrgProfile(prmStoredAccessTypes, currentOrgProfile).equals(
                    getOrgProfile(restructuredCcdAccessTypes, currentOrgProfile)
            )) {
                modifiedOrgProfileIds.add(currentOrgProfile);
                log.info("existing organisation profile/s have been modified :: {}", currentOrgProfile);
            }
        }

        // identify new organisationProfileId
        List<String> newOrgProfileIds = getOrgProfileIds(restructuredCcdAccessTypes).stream()
                .filter(el -> !getOrgProfileIds(prmStoredAccessTypes).contains(el))
                .toList();

        if (!newOrgProfileIds.isEmpty()) {
            modifiedOrgProfileIds.addAll(newOrgProfileIds);
            log.info("new organisation profile/s identified, not existing in PRM database :: {}", newOrgProfileIds);
        }

        return modifiedOrgProfileIds;
    }

    private static Set<OrganisationProfile> getOrgProfile(RestructuredAccessTypes restructuredAccessTypes,
                                                          String orgProfile) {
        return restructuredAccessTypes.getOrganisationProfiles().stream()
                .filter(organisationProfile -> organisationProfile.getOrganisationProfileId().equals(orgProfile))
                .collect(Collectors.toSet());
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
