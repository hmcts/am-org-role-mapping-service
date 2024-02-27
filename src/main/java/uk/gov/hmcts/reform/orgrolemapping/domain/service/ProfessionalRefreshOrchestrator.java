package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SUCCESS_ROLE_REFRESH;

@Service
@Slf4j
public class ProfessionalRefreshOrchestrator {

    public static final String NO_ACCESS_TYPES_FOUND = "No access types found in database";
    public static final String PRD_USER_NOT_FOUND = "User with ID %s not found in PRD";
    public static final String EXPECTED_SINGLE_PRD_USER = "Expected single user for ID %s, found %s";
    private final AccessTypesRepository accessTypesRepository;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final PRDService prdService;

    private final ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;

    public ProfessionalRefreshOrchestrator(AccessTypesRepository accessTypesRepository,
                                           UserRefreshQueueRepository userRefreshQueueRepository,
                                           PRDService prdService,
                                           ProfessionalRefreshOrchestrationHelper
                                                   professionalRefreshOrchestrationHelper) {
        this.accessTypesRepository = accessTypesRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.prdService = prdService;
        this.professionalRefreshOrchestrationHelper = professionalRefreshOrchestrationHelper;
    }

    @Transactional
    public ResponseEntity<Object> refreshProfessionalUser(String userId) {
        log.info("Single User refreshProfessionalUser for {userid}",userId);
        GetRefreshUsersResponse getRefreshUsersResponse;
        try {
            getRefreshUsersResponse = Objects.requireNonNull(prdService.getRefreshUser(userId).getBody());
        } catch (FeignException.NotFound feignClientException) {
            throw new ResourceNotFoundException(String.format(PRD_USER_NOT_FOUND, userId));
        }

        if (getRefreshUsersResponse.getUsers().size() > 1) {
            throw new ServiceException(String.format(EXPECTED_SINGLE_PRD_USER, userId,
                getRefreshUsersResponse.getUsers().size()));
        }

        professionalRefreshOrchestrationHelper.upsertUserRefreshQueue(getRefreshUsersResponse.getUsers().get(0));

        professionalRefreshOrchestrationHelper.refreshSingleUser(userRefreshQueueRepository.findByUserId(userId),
                getLatestAccessTypes());

        return ResponseEntity.ok().body(Map.of("Message", SUCCESS_ROLE_REFRESH));
    }

    @Transactional
    public void refreshProfessionalUsers() {
        professionalRefreshOrchestrationHelper.processActiveUserRefreshQueue(getLatestAccessTypes());
    }

    private AccessTypesEntity getLatestAccessTypes() {
        return accessTypesRepository.findFirstByOrderByVersionDesc().orElseThrow(
            () -> new ServiceException(NO_ACCESS_TYPES_FOUND)
        );
    }

}
