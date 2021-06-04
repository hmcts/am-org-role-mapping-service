package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseTest {
    protected static final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Force re-initialisation of base types for each test suite
    }
}
