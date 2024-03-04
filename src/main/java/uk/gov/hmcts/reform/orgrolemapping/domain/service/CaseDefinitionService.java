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
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.MAPPER;


@Service
public class CaseDefinitionService {

    private final CCDService ccdService;
    private final AccessTypesRepository accessTypesRepository;
    private final ProfileRefreshQueueRepository profileRefreshQueueRepository;
    private final ObjectMapper objectMapper;

    private final ProcessEventTracker processEventTracker;

    private final AccessTypesBuilder accessTypesBuilder = new AccessTypesBuilder();

    public CaseDefinitionService(CCDService ccdService, AccessTypesRepository accessTypesRepository,
                                 ProfileRefreshQueueRepository profileRefreshQueueRepository,
                                 ObjectMapper objectMapper, ProcessEventTracker processEventTracker) {

        this.ccdService = ccdService;
        this.accessTypesRepository = accessTypesRepository;
        this.profileRefreshQueueRepository = profileRefreshQueueRepository;
        this.objectMapper = objectMapper;
        this.processEventTracker = processEventTracker;
    }

    @Transactional
    public void findAndUpdateCaseDefinitionChanges() {

        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("PRM Process 1");
        processEventTracker.trackEventStarted(processMonitorDto);

        try {
            AccessTypesEntity localAccessTypes = accessTypesRepository.getAccessTypesEntity();

            RestructuredAccessTypes ccdAccessTypes = retrieveCCDAccessTypeDefinitions();

            RestructuredAccessTypes restructuredLocalAccessTypes =
                    restructureLocalAccessTypes(localAccessTypes.getAccessTypes());

            compareAccessTypeDefinitions(restructuredLocalAccessTypes, ccdAccessTypes);

            processMonitorDto.markAsSuccess();
        } catch (ServiceException e) {
            processMonitorDto.markAsFailed(e.getMessage()); // convert e to json string
        } finally {
            processEventTracker.trackEventCompleted(processMonitorDto);
        }
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

        return accessTypesBuilder.restructureCcdAccessTypes(Objects.requireNonNull(ccdAccessTypes.getBody()));
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

            List<String> organisationProfileIds = accessTypesBuilder
                    .identifyUpdatedOrgProfileIds(ccdAccessTypes, restructuredLocalAccessTypes);

            updateLocalDefinitions(organisationProfileIds, savedAccessTypes.getVersion());
        }

    }

    private void updateLocalDefinitions(List<String> organisationProfileIds, Long version) {

        String organisationProfileIdsString = String.join(",", organisationProfileIds);

        profileRefreshQueueRepository.upsertOrganisationProfileIds(organisationProfileIdsString, version);
    }

}
