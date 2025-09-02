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
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.OrganisationRefreshQueueValue;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrmOrganisationRefreshQueueControllerTest {

    private static final String TEST_ORGANISATION_ID = "test-id";
    private static final LocalDateTime TEST_ORGANISATION_LAST_UPDATED = LocalDateTime.now().minusDays(10);
    private static final LocalDateTime TEST_LAST_UPDATED = LocalDateTime.now().minusDays(1);
    private static final int TEST_ACCESS_TYPES_MIN_VERSION = 123;
    private static final int TEST_RETRY = 1;
    private static final LocalDateTime TEST_RETRY_AFTER = LocalDateTime.now().plusDays(1);

    @Mock
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @InjectMocks
    private PrmOrganisationRefreshQueueController controller;

    @Captor
    private ArgumentCaptor<OrganisationRefreshQueueEntity> organisationRefreshQueueEntityCaptor;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void findOrganisationRefreshQueueTest_notFound() {

        // GIVEN
        when(organisationRefreshQueueRepository.findById(TEST_ORGANISATION_ID))
            .thenReturn(Optional.empty());

        // WHEN
        var response = controller.findOrganisationRefreshQueue(TEST_ORGANISATION_ID);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(organisationRefreshQueueRepository, times(1)).findById(TEST_ORGANISATION_ID);

    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void findOrganisationRefreshQueueTest_found(boolean active) {

        // GIVEN
        when(organisationRefreshQueueRepository.findById(TEST_ORGANISATION_ID))
            .thenReturn(Optional.of(createOrganisationRefreshQueueEntity(active)));

        // WHEN
        var response = controller.findOrganisationRefreshQueue(TEST_ORGANISATION_ID);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(organisationRefreshQueueRepository, times(1)).findById(TEST_ORGANISATION_ID);

        assertOrganisationRefreshQueueValue(active, response.getBody());

    }


    @Test
    void makeOrganisationRefreshQueueActiveTest_notFound() {

        // GIVEN
        when(organisationRefreshQueueRepository.findById(TEST_ORGANISATION_ID))
            .thenReturn(Optional.empty());

        // WHEN
        var response = controller.makeOrganisationRefreshQueueActive(TEST_ORGANISATION_ID);

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(organisationRefreshQueueRepository, times(1)).findById(TEST_ORGANISATION_ID);

    }


    @Test
    void makeOrganisationRefreshQueueActiveTest_found_butActive_thenNoSave() {

        // GIVEN
        when(organisationRefreshQueueRepository.findById(TEST_ORGANISATION_ID))
            .thenReturn(Optional.of(createOrganisationRefreshQueueEntity(true))); // i.e. already active

        // WHEN
        var response = controller.makeOrganisationRefreshQueueActive(TEST_ORGANISATION_ID);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(organisationRefreshQueueRepository, times(1)).findById(TEST_ORGANISATION_ID);
        verify(organisationRefreshQueueRepository, never()).save(any()); // i.e. no save

        // verify return value is active
        assertOrganisationRefreshQueueValue(true, response.getBody());

    }


    @Test
    void makeOrganisationRefreshQueueActiveTest_found_butNotActive_thenSave() {

        // GIVEN
        when(organisationRefreshQueueRepository.findById(TEST_ORGANISATION_ID))
            .thenReturn(Optional.of(createOrganisationRefreshQueueEntity(false))); // i.e. not active

        // WHEN
        var response = controller.makeOrganisationRefreshQueueActive(TEST_ORGANISATION_ID);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(organisationRefreshQueueRepository, times(1)).findById(TEST_ORGANISATION_ID);
        verify(organisationRefreshQueueRepository, times(1)).save(organisationRefreshQueueEntityCaptor.capture());

        // verify save value is active
        var savedOrganisationRefreshQueueEntity = organisationRefreshQueueEntityCaptor.getValue();
        assertNotNull(savedOrganisationRefreshQueueEntity);
        assertEquals(TEST_ORGANISATION_ID, savedOrganisationRefreshQueueEntity.getOrganisationId());
        assertTrue(savedOrganisationRefreshQueueEntity.getActive());

        // verify return value is active
        assertOrganisationRefreshQueueValue(true, response.getBody());

    }


    private void assertOrganisationRefreshQueueValue(boolean expectedActive,
                                                     OrganisationRefreshQueueValue actualValue) {
        assertNotNull(actualValue);
        assertEquals(TEST_ORGANISATION_ID, actualValue.getOrganisationId());
        assertEquals(TEST_ORGANISATION_LAST_UPDATED, actualValue.getOrganisationLastUpdated());
        assertEquals(TEST_LAST_UPDATED, actualValue.getLastUpdated());
        assertEquals(TEST_ACCESS_TYPES_MIN_VERSION, actualValue.getAccessTypesMinVersion());
        assertEquals(TEST_RETRY, actualValue.getRetry());
        assertEquals(TEST_RETRY_AFTER, actualValue.getRetryAfter());
        assertEquals(expectedActive, actualValue.isActive());
    }

    private OrganisationRefreshQueueEntity createOrganisationRefreshQueueEntity(boolean active) {
        return OrganisationRefreshQueueEntity.builder()
            .organisationId(TEST_ORGANISATION_ID)
            .organisationLastUpdated(TEST_ORGANISATION_LAST_UPDATED)
            .lastUpdated(TEST_LAST_UPDATED)
            .accessTypesMinVersion(TEST_ACCESS_TYPES_MIN_VERSION)
            .retry(TEST_RETRY)
            .retryAfter(TEST_RETRY_AFTER)
            .active(active)
            .build();
    }

}