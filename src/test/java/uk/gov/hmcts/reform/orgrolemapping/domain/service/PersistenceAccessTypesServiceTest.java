package uk.gov.hmcts.reform.orgrolemapping.domain.service;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersistenceAccessTypesServiceTest {

    private final AccessTypesRepository accessTypeRepository = mock(AccessTypesRepository.class);

    @InjectMocks
    private final PersistenceAccessTypeService sut = new PersistenceAccessTypeService(accessTypeRepository);

    @Test
    void getAccessTypeByjsonString() {

        AccessTypes accessTypes = new AccessTypes();
        accessTypes.setAccessType("{[]}");
        when(accessTypeRepository.findByAccessType(accessTypes.getAccessType())).thenReturn(accessTypes);
        AccessTypes response = sut.getAccessType(accessTypes.getAccessType());
        assert(response.getAccessType().equals(accessTypes.getAccessType()));
    }

    @Test
    void getAccessTypeByVersion() {

        Long version = 1L;
        AccessTypes accessTypes = new AccessTypes();
        accessTypes.setAccessType("{[]}");
        when(accessTypeRepository.findByVersion(version)).thenReturn(accessTypes);
        AccessTypes response = sut.getVersion(version);
        assert(response.getAccessType().equals(accessTypes.getAccessType()));
    }
}