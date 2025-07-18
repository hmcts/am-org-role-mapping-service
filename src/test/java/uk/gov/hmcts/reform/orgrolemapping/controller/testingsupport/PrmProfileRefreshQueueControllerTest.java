package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

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
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.ProfileRefreshQueueValue;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrmProfileRefreshQueueControllerTest {

    private static final String TEST_ORGANISATION_PROFILE_ID = "test-id";
    private static final int TEST_ACCESS_TYPES_MIN_VERSION = 1;

    @Mock
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @InjectMocks
    private PrmProfileRefreshQueueController controller;

    @Captor
    private ArgumentCaptor<ProfileRefreshQueueEntity> profileRefreshQueueEntityCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void findProfileRefreshQueueTest_notFound() {

        // GIVEN
        when(profileRefreshQueueRepository.findById(TEST_ORGANISATION_PROFILE_ID))
            .thenReturn(Optional.empty());

        // WHEN
        var response = controller.findProfileRefreshQueue(TEST_ORGANISATION_PROFILE_ID);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(TEST_ORGANISATION_PROFILE_ID);

    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void findProfileRefreshQueueTest_found(boolean active) {

        // GIVEN
        when(profileRefreshQueueRepository.findById(TEST_ORGANISATION_PROFILE_ID))
            .thenReturn(Optional.of(createProfileRefreshQueueEntity(active)));

        // WHEN
        var response = controller.findProfileRefreshQueue(TEST_ORGANISATION_PROFILE_ID);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(TEST_ORGANISATION_PROFILE_ID);

        assertProfileRefreshQueueValue(active, response.getBody());

    }


    @Test
    void makeProfileRefreshQueueActiveTest_notFound() {

        // GIVEN
        String organisationProfileId = "test-id";
        when(profileRefreshQueueRepository.findById(organisationProfileId))
            .thenReturn(Optional.empty());

        // WHEN
        var response = controller.makeProfileRefreshQueueActive(organisationProfileId);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(organisationProfileId);

    }


    @Test
    void makeProfileRefreshQueueActiveTest_found_butActive_thenNoSave() {

        // GIVEN
        when(profileRefreshQueueRepository.findById(TEST_ORGANISATION_PROFILE_ID))
            .thenReturn(Optional.of(createProfileRefreshQueueEntity(true)));

        // WHEN
        var response = controller.makeProfileRefreshQueueActive(TEST_ORGANISATION_PROFILE_ID);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(TEST_ORGANISATION_PROFILE_ID);
        verify(profileRefreshQueueRepository, never()).save(any()); // i.e. no save

        // verify return value is active
        assertProfileRefreshQueueValue(true, response.getBody());

    }


    @Test
    void makeProfileRefreshQueueActiveTest_found_butNotActive_thenSave() {

        // GIVEN
        when(profileRefreshQueueRepository.findById(TEST_ORGANISATION_PROFILE_ID))
            .thenReturn(Optional.of(createProfileRefreshQueueEntity(false)));

        // WHEN
        var response = controller.makeProfileRefreshQueueActive(TEST_ORGANISATION_PROFILE_ID);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(TEST_ORGANISATION_PROFILE_ID);
        verify(profileRefreshQueueRepository, times(1)).save(profileRefreshQueueEntityCaptor.capture());

        // verify save value is active
        var savedProfileRefreshQueueEntity = profileRefreshQueueEntityCaptor.getValue();
        assertNotNull(savedProfileRefreshQueueEntity);
        assertEquals(TEST_ORGANISATION_PROFILE_ID,  savedProfileRefreshQueueEntity.getOrganisationProfileId());
        assertEquals(TEST_ACCESS_TYPES_MIN_VERSION,  savedProfileRefreshQueueEntity.getAccessTypesMinVersion());
        assertTrue(savedProfileRefreshQueueEntity.getActive());

        // verify return value is active
        assertProfileRefreshQueueValue(true, response.getBody());

    }


    private void assertProfileRefreshQueueValue(boolean expectedActive,
                                                ProfileRefreshQueueValue actualValue) {
        assertNotNull(actualValue);
        assertEquals(TEST_ORGANISATION_PROFILE_ID,  actualValue.getOrganisationProfileId());
        assertEquals(TEST_ACCESS_TYPES_MIN_VERSION,  actualValue.getAccessTypesMinVersion());
        assertEquals(expectedActive, actualValue.isActive());
    }

    private ProfileRefreshQueueEntity createProfileRefreshQueueEntity(boolean active) {
        return ProfileRefreshQueueEntity.builder()
            .organisationProfileId(TEST_ORGANISATION_PROFILE_ID)
            .accessTypesMinVersion(TEST_ACCESS_TYPES_MIN_VERSION)
            .active(active)
            .build();
    }

}
