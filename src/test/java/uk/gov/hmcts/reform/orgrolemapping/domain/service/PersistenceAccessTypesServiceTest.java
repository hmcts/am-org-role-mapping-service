package uk.gov.hmcts.reform.orgrolemapping.domain.service;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersistenceAccessTypesServiceTest {

    private final AccessTypesRepository accessTypeRepository = mock(AccessTypesRepository.class);

    @InjectMocks
    private final PersistenceAccessTypeService sut = new PersistenceAccessTypeService(accessTypeRepository);

    @Test
    void getAccessTypeByVersion() {
        Long version = 1L;
        String accessType = "{[]}";
        when(accessTypeRepository.findByAccessTypeVersion(1L)).thenReturn(accessType);
        String response = sut.fetchAccessTypeByVersion(version);
        assert(response.equals(accessType));
    }
}