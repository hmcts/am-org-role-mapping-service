package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class AccessTypeTest {

    @Test
    void getAccessTypes() {
        AccessTypesEntity accessTypesEntity = AccessTypesEntity.builder()
                .accessTypes("{[]}")
                .build();

        assertNotNull(accessTypesEntity.getAccessTypes());
        assertEquals("{[]}", accessTypesEntity.getAccessTypes());
    }

}
