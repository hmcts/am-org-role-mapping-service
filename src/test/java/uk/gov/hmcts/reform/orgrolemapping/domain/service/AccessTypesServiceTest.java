package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessTypesServiceTest {

    private final AccessTypesRepository accessTypeRepository = mock(AccessTypesRepository.class);

    @InjectMocks
    private final AccessTypeService sut = new AccessTypeService(accessTypeRepository);

    @Test
    void getAccessTypeByVersion() {

        Long version = 1L;
        AccessTypes accessTypes = new AccessTypes();
        accessTypes.setAccessTypes("{[]}");
        when(accessTypeRepository.findByVersion(version)).thenReturn(accessTypes);
        AccessTypes response = sut.getVersion(version);
        assertEquals(response.getAccessTypes(), accessTypes.getAccessTypes());
    }

    @Test
    void getAccessType() {

        AccessTypes accessTypes = new AccessTypes();
        accessTypes.setAccessTypes("{[]}");
        when(accessTypeRepository.findFirstByOrderByVersionDesc()).thenReturn(accessTypes);
        AccessTypes response = sut.getAccessTypes();
        assertEquals(response.getAccessTypes(),accessTypes.getAccessTypes());

    }
}
