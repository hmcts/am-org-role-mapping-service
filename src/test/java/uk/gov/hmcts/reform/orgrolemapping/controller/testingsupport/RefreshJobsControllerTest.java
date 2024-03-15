package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class RefreshJobsControllerTest {
    @Mock
    private PersistenceService persistenceService;
    @InjectMocks
    private final RefreshJobsController refreshJobsController = new RefreshJobsController(persistenceService);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void insertJobByIdamIds() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();
        refreshJobEntity.setJobId(1L);
        Mockito.when(persistenceService.persistRefreshJob(any()))
                .thenReturn(refreshJobEntity);
        refreshJobsController.insertJob("LEGAL_OPERATIONS",
                                        "PUBLICLAW",
                true,
                1L,
                "NEW",
                0L,
                "comments",
                userRequest);
        Mockito.verify(persistenceService, Mockito.times(2))
                .persistRefreshJob(any());
    }

    @Test
    void insertJobForService() {
        UserRequest userRequest = TestDataBuilder.buildUserRequest();
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();
        refreshJobEntity.setJobId(1L);
        refreshJobsController.insertJob("LEGAL_OPERATIONS",
                "PUBLICLAW",
                false,
                1L,
                "NEW",
                0L,
                "comments",
                userRequest);
        Mockito.verify(persistenceService, Mockito.times(1))
                .persistRefreshJob(any());
    }

    @Test
    void fetchJob() {
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.OK).body(refreshJobEntity);

        Mockito.when(persistenceService.fetchRefreshJobById(1L))
                .thenReturn(Optional.of(refreshJobEntity));
        assertEquals(response, refreshJobsController.fetchJob(1L));
    }

    @Test
    void removeJob() {
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();

        Mockito.when(persistenceService.fetchRefreshJobById(1L))
                .thenReturn(Optional.of(refreshJobEntity));

        refreshJobsController.removeJob(1L);
        Mockito.verify(persistenceService,Mockito.times(1))
                .deleteRefreshJob(refreshJobEntity);
    }

}
