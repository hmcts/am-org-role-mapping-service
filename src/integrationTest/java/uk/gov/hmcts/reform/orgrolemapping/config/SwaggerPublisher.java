package uk.gov.hmcts.reform.orgrolemapping.config;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfig;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfigRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobsRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.config.DBFlagConfigurtion;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springdoc.core.utils.Constants.DEFAULT_API_DOCS_URL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Built-in feature which saves service's swagger specs in temporary directory.
 * Each run of workflow .github/workflows/swagger.yml on master should automatically save and upload (if updated)
 * documentation.
 */
@TestPropertySource(properties = {
    // NB: hide testing-support endpoint from Swagger Publish
    "testing.support.enabled=false",
    "spring.cloud.compatibility-verifier.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=false",
    "integration-tests.use-embedded-postgres=false",
    "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
    "management.endpoint.health.group.readiness.include=ping",
    "management.health.db.enabled=false"
})
public class SwaggerPublisher extends BaseTestIntegration {

    private MockMvc mockMvc;

    @Inject
    private WebApplicationContext wac;

    @MockitoBean
    private FlagConfigRepository flagConfigRepository;

    @MockitoBean
    private EnvironmentConfiguration environmentConfiguration;

    @MockitoBean
    private RefreshJobsRepository refreshJobsRepository;

    @MockitoBean
    private DBFlagConfigurtion dbFlagConfigurtion;

    @BeforeEach
    public void setUp() {
        Mockito.when(environmentConfiguration.getEnvironment()).thenReturn("local");
        for (FeatureFlagEnum flag : FeatureFlagEnum.values()) {
            FlagConfig cfg = FlagConfig.builder()
                .flagName(flag.getValue())
                .env("local")
                .serviceName("swagger-docs")
                .status(Boolean.FALSE)
                .build();
            Mockito.when(flagConfigRepository.findByFlagNameAndEnv(flag.getValue(), "local"))
                    .thenReturn(cfg);
            Mockito.when(flagConfigRepository.findByFlagNameAndEnv(flag.getValue(), ""))
                    .thenReturn(cfg);
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void generateDocs() throws Exception {
        byte[] specs = mockMvc.perform(get(DEFAULT_API_DOCS_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        try (OutputStream outputStream = Files.newOutputStream(Paths.get("/tmp/openapi-specs.json"))) {
            outputStream.write(specs);
        }
    }

}
