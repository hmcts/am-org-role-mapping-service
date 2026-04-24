package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.IdamRecordType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.InvitationStatus;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.InvitationType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.AccountStatus;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamInvitation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.IdamFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@AllArgsConstructor
@Slf4j
public class IdamRoleMappingService {

    protected static final String INVITEUSER_NAME = "IRM Invite User";
    protected static final String NO_ENTITIES = "No entities to process";
    protected static final String QUEUE_NAME = "IRM Process %s Queue";
    protected static final String UPDATEUSER_NAME = "IRM Update User";

    private static final String EMPTY_STRING = "";
    private static final String SERVICE_NAME = "am_org_role_mapping_service";

    private final IdamFeignClient idamClient;
    private final IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;
    private final IdamRoleDataJsonBConverter idamRoleDataJsonBConverter;
    private final ProcessEventTracker processEventTracker;
    private final String retryOneIntervalMin;
    private final String retryTwoIntervalMin;
    private final String retryThreeIntervalMin;

    @Autowired
    public IdamRoleMappingService(
            IdamFeignClient idamClient,
            IdamRoleManagementQueueRepository idamRoleManagementQueueRepository,
            PlatformTransactionManager transactionManager,
            ProcessEventTracker processEventTracker,
            @Value("${idam.role.management.scheduling.retryOneIntervalMin}")
            String retryOneIntervalMin,
            @Value("${idam.role.management.scheduling.retryOneIntervalMin}")
            String retryTwoIntervalMin,
            @Value("${idam.role.management.scheduling.retryOneIntervalMin}")
            String retryThreeIntervalMin) {
        this.idamClient = idamClient;
        this.idamRoleManagementQueueRepository = idamRoleManagementQueueRepository;
        this.idamRoleDataJsonBConverter = new IdamRoleDataJsonBConverter();
        this.processEventTracker = processEventTracker;
        this.retryOneIntervalMin = retryOneIntervalMin;
        this.retryTwoIntervalMin = retryTwoIntervalMin;
        this.retryThreeIntervalMin = retryThreeIntervalMin;
    }

    @Transactional
    public void addToQueue(UserType userType, Map<String, IdamRoleData> idamRoleList) {
        log.info("Adding users to idam role mapping queue, total users: {}", idamRoleList.size());
        idamRoleList.forEach((userId, idamRoleData) -> {
            idamRoleManagementQueueRepository.upsert(userId, userType.name(),
                    idamRoleDataJsonBConverter.convertToDatabaseColumn(idamRoleData),
                    LocalDateTime.now());
        });
    }

    public ProcessMonitorDto processJudicialQueue() {
        return processQueue(UserType.JUDICIAL);
    }

    public ProcessMonitorDto processCaseWorkerQueue() {
        return processQueue(UserType.CASEWORKER);
    }

    private ProcessMonitorDto processQueue(UserType userType) {
        String queueName = String.format(QUEUE_NAME, userType.name());
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(queueName);
        processEventTracker.trackEventStarted(processMonitorDto);
        StringBuilder errorMessageBuilder = new StringBuilder();
        int successfulJobCount = 0;
        int failedJobCount = 0;
        String errorMessage;
        try {
            processMonitorDto.addProcessStep(queueName);
            boolean anyEntitiesInQueue = true;
            while (anyEntitiesInQueue) {
                // Get the next record to process and lock it.
                IdamRoleManagementQueueEntity idamRoleManagementQueueEntity
                        = idamRoleManagementQueueRepository.findAndLockSingleActiveRecord(userType.name());
                if (idamRoleManagementQueueEntity != null) {
                    errorMessage = processQueueEntry(idamRoleManagementQueueEntity);
                    if (errorMessage.isEmpty()) {
                        successfulJobCount++;
                    } else {
                        failedJobCount++;
                        errorMessageBuilder.append(errorMessage);
                    }
                }
                // If there is another record to process then continue, otherwise exit the loop.
                anyEntitiesInQueue = idamRoleManagementQueueEntity != null;
            }
            // If nothing was processed then record that fact in the process steps.
            if (successfulJobCount == 0 && failedJobCount == 0) {
                processMonitorDto.addProcessStep(NO_ENTITIES);
            }
        } catch (ServiceException ex) {
            String message = String.format("Error occurred while processing idam role mapping: %s",
                    ex.getMessage());
            log.error(message, ex);
            processMonitorDto.addProcessStep(message);
            processMonitorDto.markAsFailed(ex.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw ex;
        }
        markProcessStatus(processMonitorDto,
                successfulJobCount, failedJobCount,
                errorMessageBuilder.toString());
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    @Transactional
    private String processQueueEntry(IdamRoleManagementQueueEntity idamRoleManagementQueueEntity) {
        StringBuilder errorMessageBuilder = new StringBuilder();

        // Update the user
        ProcessMonitorDto updateProcessMonitorDto =
                updateUser(idamRoleManagementQueueEntity.getUserId(),
                        idamRoleManagementQueueEntity.getData());
        if (!EndStatus.SUCCESS.equals(updateProcessMonitorDto.getEndStatus())) {
            String message = updateProcessMonitorDto.getEndDetail();
            errorMessageBuilder.append(message);
            log.error(message);

            // Failed, so increase the retry count.
            idamRoleManagementQueueRepository.updateRetry(
                    idamRoleManagementQueueEntity.getUserId(),
                    retryOneIntervalMin, retryTwoIntervalMin, retryThreeIntervalMin);
        }
        return errorMessageBuilder.toString();
    }

    @Transactional
    public ProcessMonitorDto updateUser(String userId) {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(UPDATEUSER_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean isSuccess = false;
        // Get the idam role data
        IdamRoleData idamRoleData = getIdamRoleData(userId);
        if (idamRoleData == null) {
            String message = String.format("No idam role data found for userId %s", userId);
            errorMessageBuilder.append(message);
            log.error(message);
        } else {
            // Patch or Invite the user
            String errorMessage = patchOrInvite(userId, idamRoleData);
            if (errorMessage.isEmpty()) {
                isSuccess = true;
            } else {
                errorMessageBuilder.append(errorMessage);
            }
        }

        markProcessStatus(processMonitorDto,
                isSuccess ? 1 : 0, isSuccess ? 0 : 1,
                errorMessageBuilder.toString());
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    @Transactional
    private ProcessMonitorDto updateUser(String userId, IdamRoleData idamRoleData) {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(UPDATEUSER_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);

        // Patch or Invite the user
        String errorMessage = patchOrInvite(userId, idamRoleData);
        boolean isSuccess = errorMessage.isEmpty();

        markProcessStatus(processMonitorDto,
                isSuccess ? 1 : 0, isSuccess ? 0 : 1,
                errorMessage);
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    private String patchOrInvite(String userId, IdamRoleData idamRoleData) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean isSuccess = false;
        IdamRecordType idamRecordType = IdamRecordType.USER;
        try {
            IdamUser user = getIdamUser(userId);

            // No valid IDAM user found, so create a user object for invite.
            if  (user == null) {
                String email = idamRoleData.getEmailId();
                log.debug("No user found for userId {} ({})", userId, email);
                idamRecordType = IdamRecordType.INVITE;
                List<String> roleNames = idamRoleData.getRoles().stream()
                        .map(role -> role.getRoleName()).toList();
                // Invite the user with the roleNames.
                String errorMessage = inviteIdamUser(buildIdamUserFromEmail(userId, email), roleNames);
                if (errorMessage.isEmpty()) {
                    isSuccess = true;
                } else {
                    errorMessageBuilder.append(errorMessage);
                }
            } else {
                // Patch the user with the idam role data
                isSuccess = patchIdamUser(user, idamRoleData);
                if (!isSuccess) {
                    String message = String.format("Failed to update user with userId %s", userId);
                    errorMessageBuilder.append(message);
                    log.error(message);
                }
            }
        } catch (Exception ex) {
            String message = String.format("Error occurred while updating user with userId %s: %s",
                    userId, ex.getMessage());
            errorMessageBuilder.append(message);
            log.error(message, ex);
        }

        if (isSuccess) {
            // Set the record as published.
            idamRoleManagementQueueRepository.setAsPublished(
                    userId,
                    idamRecordType.name());
        }
        return errorMessageBuilder.toString();
    }

    private IdamRoleData getIdamRoleData(String userId) {
        Optional<IdamRoleManagementQueueEntity> irmQueueEntity =
                idamRoleManagementQueueRepository.findById(userId);
        return irmQueueEntity.isPresent() ? irmQueueEntity.get().getData() : null;
    }

    protected IdamUser getIdamUser(String userId) {
        ResponseEntity<IdamUser> response = idamClient.getUserById(userId);
        return response != null ? response.getBody() : null;
    }

    protected IdamUser getIdamUserByEmail(String email) {
        ResponseEntity<IdamUser> response = idamClient.getUserByEmail(email);
        return response != null ? response.getBody() : null;
    }

    protected boolean patchIdamUser(IdamUser user, IdamRoleData idamRoleData) {
        AtomicBoolean isPatched = new AtomicBoolean(false);
        List<String> newRoles = new ArrayList<>();
        newRoles.addAll(user.getRoleNames());

        // Update the user roles
        if ("Y".equalsIgnoreCase(idamRoleData.getDeletedFlag())) {
            // Delete user role
            idamRoleData.getRoles().forEach(roleData -> {
                if (newRoles.contains(roleData.getRoleName())) {
                    newRoles.remove(roleData.getRoleName());
                    isPatched.set(true);
                }
            });
        } else {
            // Add user role
            idamRoleData.getRoles().forEach(roleData -> {
                if (!newRoles.contains(roleData.getRoleName())) {
                    newRoles.add(roleData.getRoleName());
                    isPatched.set(true);
                }
            });
        }

        // Update status
        AccountStatus newAccountStatus = getIdamUserAccountStatus(idamRoleData.getActiveFlag());
        if (!newAccountStatus.equals(user.getAccountStatus())) {
            user.setAccountStatus(newAccountStatus);
            isPatched.set(true);
        }

        if  (!isPatched.get()) {
            // No data to patch, so return success without calling idam.
            log.debug("Idam role data unchanged for userId {}", user.getId());
            return true;
        } else {
            // Update the idam user
            user.setRoleNames(newRoles);
            log.debug("Idam role data patched for userId {}", user.getId());
            ResponseEntity<IdamUser> response = idamClient.updateUser(user.getId(), user);
            return HttpStatus.OK.equals(response.getStatusCode());
        }
    }

    private static AccountStatus getIdamUserAccountStatus(String activeFlag) {
        return "N".equalsIgnoreCase(activeFlag) ? AccountStatus.SUSPENDED : AccountStatus.ACTIVE;
    }

    @Transactional
    public ProcessMonitorDto inviteUser(String email, List<String> roleNames) {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(INVITEUSER_NAME);
        processEventTracker.trackEventStarted(processMonitorDto);
        StringBuilder errorMessageBuilder = new StringBuilder();
        boolean isSuccess = false;

        // Check for a valid IDAM user with this email.
        IdamUser user = getIdamUserByEmail(email);

        // No valid IDAM user found, so create a user object for invite.
        if (user == null) {
            log.debug("No user found for email {}", email);
            user = buildIdamUserFromEmail(null, email);
        }

        // Invite the user on IDAM.
        String errorMessage = inviteIdamUser(user, roleNames);
        if (errorMessage.isEmpty()) {
            isSuccess = true;
        } else {
            errorMessageBuilder.append(errorMessage);
        }

        markProcessStatus(processMonitorDto,
                isSuccess ? 1 : 0, isSuccess ? 0 : 1,
                errorMessageBuilder.toString());
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    protected String inviteIdamUser(IdamUser user, List<String> roleNames) {
        StringBuilder errorMessageBuilder = new StringBuilder();

        try {
            // Check for any existing invitations
            List<IdamInvitation> invitations = getIdamUserInvitations(user);

            // Remove any existing invitations
            deleteIdamUserInvitations(invitations);

            // Create a new invitation
            boolean isSuccess = createInvitation(user, roleNames);
            if (!isSuccess) {
                String message = String.format("Failed to invite userId %s", user.getId());
                errorMessageBuilder.append(message);
                log.error(message);
            }
        } catch (Exception ex) {
            String message = String.format("Error occurred during invite for userId %s: %s",
                    user.getId(), ex.getMessage());
            errorMessageBuilder.append(message);
            log.error(message, ex);
        }

        return errorMessageBuilder.toString();
    }

    private List<IdamInvitation> getIdamUserInvitations(IdamUser user) {
        ResponseEntity<List<IdamInvitation>> response = idamClient.getInvitations(user.getEmail());
        List<IdamInvitation> invitations = HttpStatus.OK.equals(response.getStatusCode())
                ? response.getBody() : Collections.emptyList();
        log.debug("{} Invitations found for userId {}", invitations.size(), user.getId());
        return invitations;
    }

    private void deleteIdamUserInvitations(List<IdamInvitation> invitations) {
        invitations.forEach(invitation -> {
            log.debug("Removing invitation with id {}", invitation.getId());
            idamClient.deleteInvitation(invitation.getId());
        });
    }

    private boolean createInvitation(IdamUser user, List<String> roleNames) {
        final IdamInvitation invitation = buildInvitationFromUser(user, roleNames);
        ResponseEntity<IdamInvitation> response = idamClient.inviteUser(invitation);
        log.debug("Created invitation with id {}", invitation.getId());
        return HttpStatus.CREATED.equals(response.getStatusCode());
    }

    protected IdamUser buildIdamUserFromEmail(String userId, String email) {
        return IdamUser.builder()
                .id(userId)
                .email(email)
                .build();

    }

    protected IdamInvitation buildInvitationFromUser(IdamUser user, List<String> roleNames) {
        return IdamInvitation.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .forename(user.getForename())
                .surname(user.getSurname())
                .activationRoleNames(roleNames)
                .invitationType(InvitationType.APPOINT)
                .invitationStatus(InvitationStatus.PENDING)
                .clientId(SERVICE_NAME)
                .successRedirect(EMPTY_STRING)
                .invitedBy(EMPTY_STRING)
                .build();
    }

    private void markProcessStatus(ProcessMonitorDto processMonitorDto, int successfulJobCount,
                                   int failedJobCount, String errorMessage) {
        boolean hasSuccessfulStep = successfulJobCount > 0 || (successfulJobCount == 0 && failedJobCount == 0);
        boolean hasFailedAStep = failedJobCount > 0;
        if (!hasSuccessfulStep && hasFailedAStep) {
            processMonitorDto.markAsFailed(errorMessage);
        }

        if (hasSuccessfulStep && hasFailedAStep) {
            processMonitorDto.markAsPartialSuccess(errorMessage);
        }

        if (hasSuccessfulStep && !hasFailedAStep) {
            processMonitorDto.markAsSuccess();
        }
    }
}
