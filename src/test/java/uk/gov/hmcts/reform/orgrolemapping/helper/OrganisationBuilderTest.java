package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrganisationBuilderTest {

    @Test
    void buildOrganisationProfileResponse() {

        OrganisationsResponse organisationsResponse = OrganisationBuilder
                .buildOrganisationProfileResponse("organisationsResponseSample.json");
        assertNotNull(organisationsResponse);
        assertEquals(2, organisationsResponse.getOrganisations().size());
        assertEquals("1", organisationsResponse.getOrganisations().get(0).getOrganisationIdentifier());
        assertEquals("2", organisationsResponse.getOrganisations().get(1).getOrganisationIdentifier());
    }

}
