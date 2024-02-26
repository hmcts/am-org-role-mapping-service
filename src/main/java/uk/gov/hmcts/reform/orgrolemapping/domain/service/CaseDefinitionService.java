package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.helper.AccessTypesBuilder;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.MAPPER;


@Service
public class CaseDefinitionService {

    private final CCDService ccdService;
    private final AccessTypesRepository accessTypesRepository;
    private final ProfileRefreshQueueRepository profileRefreshQueueRepository;
    private final ObjectMapper objectMapper;

    public CaseDefinitionService(CCDService ccdService, AccessTypesRepository accessTypesRepository,
                                 ProfileRefreshQueueRepository profileRefreshQueueRepository,
                                 ObjectMapper objectMapper) {

        this.ccdService = ccdService;
        this.accessTypesRepository = accessTypesRepository;
        this.profileRefreshQueueRepository = profileRefreshQueueRepository;
        this.objectMapper = objectMapper;

    }

    @Transactional
    public void findAndUpdateCaseDefinitionChanges() {

        AccessTypesEntity localAccessTypes = accessTypesRepository.getAccessTypesEntity();

        RestructuredAccessTypes ccdAccessTypes = retrieveCCDAccessTypeDefinitions();

        RestructuredAccessTypes restructuredLocalAccessTypes =
                restructureLocalAccessTypes(localAccessTypes.getAccessTypes());

        compareAccessTypeDefinitions(restructuredLocalAccessTypes, ccdAccessTypes);

    }

    private RestructuredAccessTypes restructureLocalAccessTypes(String localAccessTypes) {

        try {
            return MAPPER.readValue(localAccessTypes, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new ServiceException(String.format("Unable to serialize access types %s", localAccessTypes), e);
        }

    }

    private RestructuredAccessTypes retrieveCCDAccessTypeDefinitions() {

        ResponseEntity<AccessTypesResponse> ccdAccessTypes = ccdService.fetchAccessTypes();
        
        return AccessTypesBuilder.restructureCcdAccessTypes(Objects.requireNonNull(ccdAccessTypes.getBody()));
    }

    private void compareAccessTypeDefinitions(
            RestructuredAccessTypes restructuredLocalAccessTypes,
            RestructuredAccessTypes ccdAccessTypes) {

        if (!restructuredLocalAccessTypes.equals(ccdAccessTypes)) {

            String ccdAccessTypesString;
            try {
                ccdAccessTypesString = objectMapper.writeValueAsString(ccdAccessTypes);
            } catch (JsonProcessingException e) {
                throw new ServiceException(String.format("Unable to serialize access types %s", ccdAccessTypes), e);
            }

            AccessTypesEntity savedAccessTypes = accessTypesRepository
                    .updateAccessTypesEntity(ccdAccessTypesString);

            List<String> organisationProfileIds = AccessTypesBuilder
                    .identifyUpdatedOrgProfileIds(ccdAccessTypes, restructuredLocalAccessTypes);

            updateLocalDefinitions(organisationProfileIds, savedAccessTypes.getVersion());
        }

    }

    private void updateLocalDefinitions(List<String> organisationProfileIds, Long version) {

        String organisationProfileIdsString = String.join(",", organisationProfileIds);

        profileRefreshQueueRepository.upsertOrganisationProfileIds(organisationProfileIdsString, version);
    }

}
