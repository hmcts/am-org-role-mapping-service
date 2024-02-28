package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.*;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.*;

@Transactional
public class ProfessionalUserServiceTest extends BaseTestIntegration {

    @Autowired
    private ProfessionalUserService professionalUserService;

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @MockBean
    private PrdService prdService;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql",
            "classpath:sql/insert_batch_last_run.sql", "classpath:sql/insert_user_refresh_queue.sql"})
    void ShouldFindUserChangesAndInsertIntoRefreshQueue_WithoutPagination() {
        RefreshUser refreshUser = refreshUser(1);
        GetRefreshUserResponse response1 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser, "123", false);

        when(prdService.retrieveUsers(any(), anyInt(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        assertEquals(2, userRefreshQueueRepository.findAll().size()); //TODO: One from insert_user_sql, one from this run
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql",
            "classpath:sql/insert_batch_last_run.sql", "classpath:sql/insert_user_refresh_queue.sql"})
    void ShouldFindUserChangesAndInsertIntoRefreshQueue_WithPagination() {
        RefreshUser refreshUser = refreshUser(1);
        GetRefreshUserResponse response1 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser, "123", true);

        when(prdService.retrieveUsers(any(), anyInt(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

        RefreshUser refreshUser2 = refreshUser(2);
        GetRefreshUserResponse response2 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser2, "456", false);

        when(prdService.retrieveUsers(any(), anyInt(), any(String.class)))
                .thenReturn(ResponseEntity.ok(response2));

        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        assertEquals(3, userRefreshQueueRepository.findAll().size()); //TODO: One from insert_user_sql, two from this run
    }

}




//TODO: delete more of this, it's been moved into IntTestDataBuilder

//        OrganisationInfo organisationInfo = OrganisationInfo.builder()
//                .organisationIdentifier("321")
//                .status("ACTIVE")
//                .lastUpdated(LocalDateTime.now())
//                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();
//
//        UserAccessTypes userAccessTypes = UserAccessTypes.builder()
//                .accessTypeId("123")
//                .jurisdictionId("123")
//                .organisationProfileId("123")
//                .enabled("true")
//                .build();
//
//        RefreshUser refreshUser = RefreshUser.builder()
//                .userIdentifier("123")
//                .lastUpdated(LocalDateTime.now())
//                .organisationInfo(organisationInfo)
//                .userAccessTypes(List.of(userAccessTypes))
//                .build();

//        GetRefreshUserResponse response = GetRefreshUserResponse.builder()
//                .users(List.of(refreshUser))
//                .lastRecordInPage("123")
//                .moreAvailable(false)
//                .build();
