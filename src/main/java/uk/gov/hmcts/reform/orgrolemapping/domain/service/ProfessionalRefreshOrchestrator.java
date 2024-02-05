package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private final ObjectMapper objectMapper;

    public ProfessionalRefreshOrchestrator(AccessTypesRepository accessTypesRepository,
                                           UserRefreshQueueRepository userRefreshQueueRepository,
                                           PRDService prdService,
                                           ObjectMapper objectMapper) {
        this.accessTypesRepository = accessTypesRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.prdService = prdService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResponseEntity<Object> refreshProfessionalUser(String userId) {
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

        upsertUserRefreshQueue(getRefreshUsersResponse.getUsers().get(0));

        refreshSingleUser(userRefreshQueueRepository.findByUserId(userId), getLatestAccessTypes());

        return ResponseEntity.ok().body(Map.of("Message", SUCCESS_ROLE_REFRESH));
    }

    @Transactional
    public void refreshProfessionalUsers() {
        processActiveUserRefreshQueue(getLatestAccessTypes());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void upsertUserRefreshQueue(RefreshUser prdUser) {
        String userAccessTypes = null;
        try {
            userAccessTypes = objectMapper.writeValueAsString(prdUser.getUserAccessTypes());
        } catch (JsonProcessingException e) {
            throw new ServiceException(String.format("Unable to serialize user access types for PRD user %s", prdUser.getUserIdentifier()), e);
        }

        userRefreshQueueRepository.upsert(
            prdUser.getUserIdentifier(),
            prdUser.getLastUpdated(),
            getLatestAccessTypes().getVersion(),
            prdUser.getDateTimeDeleted(),
            userAccessTypes,
            prdUser.getOrganisationInfo().getOrganisationIdentifier(),
            prdUser.getOrganisationInfo().getStatus().name(),
            String.join(",", prdUser.getOrganisationInfo().getOrganisationProfileIds())
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void processActiveUserRefreshQueue(AccessTypesEntity accessTypes) {
        Optional<UserRefreshQueueEntity> userRefreshQueue = userRefreshQueueRepository.findFirstByActiveTrue();
        if (userRefreshQueue.isEmpty()) {
            return;
        }

        refreshSingleUser(userRefreshQueue.get(), accessTypes);

        // Process any further record(s) recursively
        processActiveUserRefreshQueue(accessTypes);
    }

    private AccessTypesEntity getLatestAccessTypes() {
        return accessTypesRepository.findFirstByOrderByVersionDesc().orElseThrow(
            () -> new ServiceException(NO_ACCESS_TYPES_FOUND)
        );
    }

    private void refreshSingleUser(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        log.debug("Refreshing professional role assignments for user '{}'", userRefreshQueue.getUserId());
        generateRoleAssignments(userRefreshQueue, accessTypes);
        userRefreshQueue.setActive(false);
    }

    private void generateRoleAssignments(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        // TODO: GA-138
    }
}
