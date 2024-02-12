package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;

import java.io.File;

public class ProfessionalUserBuilder {

    public static UsersByOrganisationResponse buildUsersByOrganisationResponse(String resource) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(
                    new File("src/main/resources/" + resource),
                    UsersByOrganisationResponse.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid sample json file or missing.");
        }
    }
}
