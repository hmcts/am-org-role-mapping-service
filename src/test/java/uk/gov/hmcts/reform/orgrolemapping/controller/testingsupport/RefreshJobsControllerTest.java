package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.RefreshJob;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        // GIVEN
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();
        // fully populate entity: so we have at least one test to fully validate output values using assertRefreshJob
        refreshJobEntity.setJobId(2L);
        refreshJobEntity.setRoleCategory("test role category");
        refreshJobEntity.setJurisdiction("test jurisdiction");
        refreshJobEntity.setStatus("test status");
        refreshJobEntity.setComments("test comments");
        refreshJobEntity.setUserIds(new String[]{"test user id 1", "test user id 1"});
        refreshJobEntity.setLog("test log");
        refreshJobEntity.setLinkedJobId(1L);
        refreshJobEntity.setCreated(ZonedDateTime.now());
        Mockito.when(persistenceService.persistRefreshJob(any()))
                .thenReturn(refreshJobEntity);
        UserRequest userRequest = TestDataBuilder.buildUserRequest();

        // WHEN
        ResponseEntity<RefreshJob> response = refreshJobsController.insertJob("LEGAL_OPERATIONS",
                "PUBLICLAW",
                true,
                null, // to be defaulted
                null, // to be defaulted
                "comments",
                userRequest);

        // THEN
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertRefreshJob(refreshJobEntity, response.getBody());

        ArgumentCaptor<RefreshJobEntity> refreshJobCaptor = ArgumentCaptor.forClass(RefreshJobEntity.class);
        // NB: persistRefreshJob is called twice: once to create job ID and once to set job ID
        Mockito.verify(persistenceService, Mockito.times(2)).persistRefreshJob(refreshJobCaptor.capture());
        List<RefreshJobEntity> refreshJobValues = refreshJobCaptor.getAllValues();
        // verify values in first save
        RefreshJobEntity refreshJob1 = refreshJobValues.get(0);
        assertEquals("LEGAL_OPERATIONS", refreshJob1.getRoleCategory());
        assertEquals("PUBLICLAW", refreshJob1.getJurisdiction());
        assertNull(refreshJob1.getLinkedJobId()); // NB: to be defaulted in save number 2
        assertEquals("NEW", refreshJob1.getStatus()); // NB: defaulted
        assertEquals("comments", refreshJob1.getComments());
        assertEquals(userRequest.getUserIds().size(), refreshJob1.getUserIds().length);
        assertNotNull(refreshJob1.getCreated()); // NB: defaulted as not in request
        // verify second save has updated linked Job ID to match job ID from output of first save
        RefreshJobEntity refreshJob2 = refreshJobValues.get(1);
        assertEquals(refreshJobEntity.getJobId(), refreshJob2.getLinkedJobId());
    }

    @Test
    void insertJobForService() {

        // GIVEN
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();
        refreshJobEntity.setJobId(1L);
        Mockito.when(persistenceService.persistRefreshJob(any()))
                .thenReturn(refreshJobEntity);

        // WHEN
        ResponseEntity<RefreshJob> response = refreshJobsController.insertJob("LEGAL_OPERATIONS",
                "PUBLICLAW",
                false,
                1L,
                "NEW",
                "comments",
                null);

        // THEN
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertRefreshJob(refreshJobEntity, response.getBody());
        Mockito.verify(persistenceService, Mockito.times(1)).persistRefreshJob(any());
    }

    @Test
    void fetchJob() {

        // GIVEN
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();
        Mockito.when(persistenceService.fetchRefreshJobById(1L))
                .thenReturn(Optional.of(refreshJobEntity));

        // WHEN
        ResponseEntity<RefreshJob> response = refreshJobsController.fetchJob(1L);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(refreshJobEntity.getJobId(), response.getBody().getJobId());
    }

    @Test
    void fetchJob_whenMissingJob() {

        // GIVEN
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();
        Mockito.when(persistenceService.fetchRefreshJobById(1L))
                .thenReturn(Optional.empty());

        // WHEN
        ResponseEntity<RefreshJob> response = refreshJobsController.fetchJob(1L);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void removeJob() {
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();

        Mockito.when(persistenceService.fetchRefreshJobById(1L))
                .thenReturn(Optional.of(refreshJobEntity));

        // WHEN
        ResponseEntity<Void> response = refreshJobsController.removeJob(1L);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        Mockito.verify(persistenceService,Mockito.times(1)).deleteRefreshJob(refreshJobEntity);
    }

    @Test
    void removeJob_whenMissingJob() {
        RefreshJobEntity refreshJobEntity = new RefreshJobEntity();

        Mockito.when(persistenceService.fetchRefreshJobById(1L))
                .thenReturn(Optional.empty());

        // WHEN
        ResponseEntity<Void> response = refreshJobsController.removeJob(1L);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        // NB: delete skipped but response still the same
        Mockito.verify(persistenceService,Mockito.never()).deleteRefreshJob(refreshJobEntity);
    }

    private void assertRefreshJob(RefreshJobEntity expectedRJEntity, RefreshJob actualRefreshJob) {
        assertEquals(expectedRJEntity.getJobId(), actualRefreshJob.getJobId());
        assertEquals(expectedRJEntity.getRoleCategory(), actualRefreshJob.getRoleCategory());
        assertEquals(expectedRJEntity.getJurisdiction(), actualRefreshJob.getJurisdiction());
        assertEquals(expectedRJEntity.getStatus(), actualRefreshJob.getStatus());
        assertEquals(expectedRJEntity.getComments(), actualRefreshJob.getComments());
        if (expectedRJEntity.getUserIds() == null) {
            assertNull(actualRefreshJob.getUserIds());
        } else {
            assertEquals(expectedRJEntity.getUserIds().length, actualRefreshJob.getUserIds().length);
            assertThat(Arrays.stream(expectedRJEntity.getUserIds()).toList(),
                    containsInAnyOrder(actualRefreshJob.getUserIds()));
        }
        assertEquals(expectedRJEntity.getLog(), actualRefreshJob.getLog());
        assertEquals(expectedRJEntity.getLinkedJobId(), actualRefreshJob.getLinkedJobId());
        if (expectedRJEntity.getCreated() == null) {
            assertNull(actualRefreshJob.getCreated());
        } else {
            assertTrue(expectedRJEntity.getCreated().isEqual(actualRefreshJob.getCreated()));
        }
    }

}
