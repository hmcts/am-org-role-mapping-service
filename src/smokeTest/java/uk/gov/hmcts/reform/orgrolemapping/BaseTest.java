package uk.gov.hmcts.reform.orgrolemapping;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.jackson.JacksonEncoder;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

@ExtendWith(SerenityJUnit5Extension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseTest {

    private static final String POSTGRES = "postgres";
    protected static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public ServiceAuthorisationApi generateServiceAuthorisationApi(final String s2sUrl) {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .contract(new SpringMvcContract())
                .target(ServiceAuthorisationApi.class, s2sUrl);
    }

    public ServiceAuthTokenGenerator authTokenGenerator(
            final String secret,
            final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi) {
        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }


    @TestConfiguration
    static class Configuration implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            final PostgreSQLContainer pg = new PostgreSQLContainer()
                    .withDatabaseName(POSTGRES)
                    .withUsername(POSTGRES)
                    .withPassword(POSTGRES);
            pg.start();

            TestPropertyValues.of(
                    "spring.datasource.url=" + pg.getJdbcUrl(),
                    "spring.datasource.username=" + pg.getUsername(),
                    "spring.datasource.password=" + pg.getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}

