package uk.gov.hmcts.reform.orgrolemapping.helper;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AccessTypesBuilder {

    public RestructuredAccessTypes restructureCcdAccessTypes(AccessTypesResponse accessTypesResponse) {
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

                organisationProfilesAccessTypesMap
                        .computeIfAbsent(orgProfileTempKey, k -> new HashSet<>()).add(orgProfileAccessType);
            }

            organisationProfilesAccessTypesMap.forEach((key, val) -> {
                OrganisationProfileJurisdiction orgProfileJurisdiction = new OrganisationProfileJurisdiction();
                orgProfileJurisdiction.setJurisdictionId(jurisdictionTempKey);
                orgProfileJurisdiction.setAccessTypes(val);

                organisationProfilesMap.computeIfAbsent(key, k -> new HashSet<>()).add(orgProfileJurisdiction);
            });
        }

        Set<OrganisationProfile> organisationProfileList = organisationProfilesMap.keySet().stream()
                .map(key -> new OrganisationProfile(key, organisationProfilesMap.get(key)))
                .collect(Collectors.toSet());

        return new RestructuredAccessTypes(organisationProfileList);
    }

    public List<String> identifyUpdatedOrgProfileIds(RestructuredAccessTypes restructuredCcdAccessTypes,
                                                     RestructuredAccessTypes prmStoredAccessTypes) {
        if (prmStoredAccessTypes.getOrganisationProfiles() == null) {
            log.debug("no organisation profile/s in PRM database, hence returning all from CCD");
            return getOrgProfileIds(restructuredCcdAccessTypes);
        }

        List<String> modifiedOrgProfileIds = new ArrayList<>();

        // compare each PRM stored `org profile` with CCD representation of `org profile`
        for (OrganisationProfile organisationProfile : prmStoredAccessTypes.getOrganisationProfiles()) {
            String currentOrgProfile = organisationProfile.getOrganisationProfileId();

            if (!Set.of(organisationProfile).equals(getOrgProfile(restructuredCcdAccessTypes, currentOrgProfile))) {
                modifiedOrgProfileIds.add(currentOrgProfile);
                log.debug("existing organisation profile/s have been modified :: {}", currentOrgProfile);
            }
        }

        // identify new organisationProfileId
        List<String> newOrgProfileIds = getOrgProfileIds(restructuredCcdAccessTypes).stream()
                .filter(orgProfile -> !getOrgProfileIds(prmStoredAccessTypes).contains(orgProfile))
                .toList();

        if (!newOrgProfileIds.isEmpty()) {
            modifiedOrgProfileIds.addAll(newOrgProfileIds);
            log.debug("new organisation profile/s identified, not existing in PRM database :: {}", newOrgProfileIds);
        }

        return modifiedOrgProfileIds;
    }

    private Set<OrganisationProfile> getOrgProfile(RestructuredAccessTypes restructuredAccessTypes,
                                                   String orgProfile) {
        return restructuredAccessTypes.getOrganisationProfiles().stream()
                .filter(organisationProfile -> organisationProfile.getOrganisationProfileId().equals(orgProfile))
                .collect(Collectors.toSet());
    }

    private List<String> getOrgProfileIds(RestructuredAccessTypes restructuredAccessTypes) {
        return restructuredAccessTypes.getOrganisationProfiles().stream()
                .map(OrganisationProfile::getOrganisationProfileId)
                .toList();
    }

}
