package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.UserRefreshQueueValue;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;

class PrmUserRefreshQueueControllerTest {

    private static final String TEST_USER_ID = "test-userId";
    private static final int TEST_ACCESS_TYPES_MIN_VERSION = 1;

    @Mock
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @InjectMocks
    private PrmUserRefreshQueueController controller;

    @Captor
    private ArgumentCaptor<UserRefreshQueueEntity> userRefreshQueueEntityCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void findUserRefreshQueueTest_notFound() {

        // GIVEN
        when(userRefreshQueueRepository.findById(TEST_USER_ID))
            .thenReturn(Optional.empty());

        // WHEN
        var response = controller.findUserRefreshQueue(TEST_USER_ID);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(userRefreshQueueRepository, times(1)).findById(TEST_USER_ID);
    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void findUserRefreshQueueTest_found(boolean active) {

        // GIVEN
        when(userRefreshQueueRepository.findById(TEST_USER_ID))
            .thenReturn(Optional.of(createUserRefreshQueueEntity(active)));

        // WHEN
        var response = controller.findUserRefreshQueue(TEST_USER_ID);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(userRefreshQueueRepository, times(1)).findById(TEST_USER_ID);

        assertUserRefreshQueueValue(active, response.getBody());

    }


    @Test
    void makeUserRefreshQueueActiveTest_notFound() {

        // GIVEN
        String organisationUserId = "test-id";
        when(userRefreshQueueRepository.findById(organisationUserId))
            .thenReturn(Optional.empty());

        // WHEN
        var response = controller.makeUserRefreshQueueActive(organisationUserId);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(userRefreshQueueRepository, times(1)).findById(organisationUserId);
    }


    @Test
    void makeUserRefreshQueueActiveTest_found_butActive_thenNoSave() {

        // GIVEN
        when(userRefreshQueueRepository.findById(TEST_USER_ID))
            .thenReturn(Optional.of(createUserRefreshQueueEntity(true)));

        // WHEN
        var response = controller.makeUserRefreshQueueActive(TEST_USER_ID);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(userRefreshQueueRepository, times(1)).findById(TEST_USER_ID);
        verify(userRefreshQueueRepository, never()).save(any()); // i.e. no save

        // verify return value is active
        assertUserRefreshQueueValue(true, response.getBody());

    }


    @Test
    void makeUserRefreshQueueActiveTest_found_butNotActive_thenSave() {

        // GIVEN
        when(userRefreshQueueRepository.findById(TEST_USER_ID))
            .thenReturn(Optional.of(createUserRefreshQueueEntity(false)));

        // WHEN
        var response = controller.makeUserRefreshQueueActive(TEST_USER_ID);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(userRefreshQueueRepository, times(1)).findById(TEST_USER_ID);
        verify(userRefreshQueueRepository, times(1)).save(userRefreshQueueEntityCaptor.capture());

        // verify save value is active
        var savedUserRefreshQueueEntity = userRefreshQueueEntityCaptor.getValue();
        assertNotNull(savedUserRefreshQueueEntity);
        assertEquals(TEST_USER_ID,  savedUserRefreshQueueEntity.getUserId());
        assertEquals(TEST_ACCESS_TYPES_MIN_VERSION,  savedUserRefreshQueueEntity.getAccessTypesMinVersion());
        assertTrue(savedUserRefreshQueueEntity.getActive());

        // verify return value is active
        assertUserRefreshQueueValue(true, response.getBody());

    }


    private void assertUserRefreshQueueValue(boolean expectedActive,
        UserRefreshQueueValue actualValue) {
        assertNotNull(actualValue);
        assertEquals(TEST_USER_ID,  actualValue.getUserId());
        assertEquals(TEST_ACCESS_TYPES_MIN_VERSION,  actualValue.getAccessTypesMinVersion());
        assertEquals(expectedActive, actualValue.isActive());
    }

    private UserRefreshQueueEntity createUserRefreshQueueEntity(boolean active) {
        return UserRefreshQueueEntity.builder()
            .userId(TEST_USER_ID)
            .retry(0)
            .accessTypesMinVersion(TEST_ACCESS_TYPES_MIN_VERSION)
            .active(active)
            .build();
    }

}
