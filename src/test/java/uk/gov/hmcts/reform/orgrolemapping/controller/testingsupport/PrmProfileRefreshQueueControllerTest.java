package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
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
        String organisationProfileId = "test-id";
        when(profileRefreshQueueRepository.findById(organisationProfileId)).thenReturn(Optional.empty());

        // WHEN
        var response = controller.findProfileRefreshQueue(organisationProfileId);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(organisationProfileId);

    }


    @Test
    void findProfileRefreshQueueTest_found() {

        // GIVEN
        String organisationProfileId = "test-id";
        Integer accessTypesMinVersion = 1;
        Boolean active = true;
        var profileRefreshQueueEntity = ProfileRefreshQueueEntity.builder()
            .organisationProfileId(organisationProfileId)
            .accessTypesMinVersion(accessTypesMinVersion)
            .active(active)
            .build();

        when(profileRefreshQueueRepository.findById(organisationProfileId))
            .thenReturn(Optional.of(profileRefreshQueueEntity));

        // WHEN
        var response = controller.findProfileRefreshQueue(organisationProfileId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(organisationProfileId);

        assertNotNull(response.getBody());
        assertEquals(organisationProfileId,  response.getBody().getOrganisationProfileId());
        assertEquals(accessTypesMinVersion,  response.getBody().getAccessTypesMinVersion());
        assertEquals(active,  response.getBody().getActive());

    }

    @Test
    void makeProfileRefreshQueueActiveTest_notFound() {

        // GIVEN
        String organisationProfileId = "test-id";
        when(profileRefreshQueueRepository.findById(organisationProfileId)).thenReturn(Optional.empty());

        // WHEN
        var response = controller.makeProfileRefreshQueueActive(organisationProfileId);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(organisationProfileId);

    }


    @Test
    void makeProfileRefreshQueueActiveTest_found_butActive_thenNoSave() {

        // GIVEN
        String organisationProfileId = "test-id";
        Integer accessTypesMinVersion = 1;
        Boolean active = true;
        var profileRefreshQueueEntity = ProfileRefreshQueueEntity.builder()
            .organisationProfileId(organisationProfileId)
            .accessTypesMinVersion(accessTypesMinVersion)
            .active(active)
            .build();

        when(profileRefreshQueueRepository.findById(organisationProfileId))
            .thenReturn(Optional.of(profileRefreshQueueEntity));

        // WHEN
        var response = controller.makeProfileRefreshQueueActive(organisationProfileId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(organisationProfileId);
        verify(profileRefreshQueueRepository, never()).save(any());

        assertNotNull(response.getBody());
        assertEquals(organisationProfileId,  response.getBody().getOrganisationProfileId());
        assertEquals(accessTypesMinVersion,  response.getBody().getAccessTypesMinVersion());
        assertEquals(true,  response.getBody().getActive());

    }


    @Test
    void makeProfileRefreshQueueActiveTest_found_butNotActive_thenSave() {

        // GIVEN
        String organisationProfileId = "test-id";
        Integer accessTypesMinVersion = 1;
        Boolean active = false;
        var profileRefreshQueueEntity = ProfileRefreshQueueEntity.builder()
            .organisationProfileId(organisationProfileId)
            .accessTypesMinVersion(accessTypesMinVersion)
            .active(active)
            .build();

        when(profileRefreshQueueRepository.findById(organisationProfileId))
            .thenReturn(Optional.of(profileRefreshQueueEntity));

        // WHEN
        var response = controller.makeProfileRefreshQueueActive(organisationProfileId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(profileRefreshQueueRepository, times(1)).findById(organisationProfileId);
        verify(profileRefreshQueueRepository, times(1)).save(profileRefreshQueueEntityCaptor.capture());

        var savedProfileRefreshQueueEntity = profileRefreshQueueEntityCaptor.getValue();
        assertEquals(organisationProfileId,  savedProfileRefreshQueueEntity.getOrganisationProfileId());
        assertEquals(accessTypesMinVersion,  savedProfileRefreshQueueEntity.getAccessTypesMinVersion());
        assertTrue(savedProfileRefreshQueueEntity.getActive());

        assertNotNull(response.getBody());
        assertEquals(organisationProfileId,  response.getBody().getOrganisationProfileId());
        assertEquals(accessTypesMinVersion,  response.getBody().getAccessTypesMinVersion());
        assertEquals(true,  response.getBody().getActive());

    }

}
