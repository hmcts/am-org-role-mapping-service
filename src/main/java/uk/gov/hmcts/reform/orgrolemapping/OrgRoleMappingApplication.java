package uk.gov.hmcts.reform.orgrolemapping;

import feign.Feign;
import feign.jackson.JacksonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

@SpringBootApplication
@EnableCircuitBreaker
@EnableFeignClients
@ConfigurationProperties
public class OrgRoleMappingApplication implements CommandLineRunner {

    @Autowired
    private Environment env;
    private static final Logger logger = LoggerFactory.getLogger(OrgRoleMappingApplication.class);

    public static void main(final String[] args) {
        SpringApplication.run(OrgRoleMappingApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.error("Start printing env variables");
        logger.error("{}", env);
        logger.error("Host is : {}", env.getProperty("ORG_ROLE_MAPPING_SERVICE_POSTGRES_HOST"));
        logger.error(" User is: {}", env.getProperty("ORG_ROLE_MAPPING_SERVICE_POSTGRES_USER"));
        logger.error(" Password is: {}", env.getProperty("ORG_ROLE_MAPPING_SERVICE_POSTGRES_PASS"));
        logger.error(" Port is : {}", env.getProperty("ORG_ROLE_MAPPING_SERVICE_POSTGRES_PORT"));
        logger.error("End printing env variables");


        System.out.println("Start printing env variables");

        System.out.println("Host is : " + env.getProperty("ORG_ROLE_MAPPING_SERVICE_POSTGRES_HOST"));
        System.out.println(" User is: " + env.getProperty("ORG_ROLE_MAPPING_SERVICE_POSTGRES_USER"));
        System.out.println(" Password is: " + env.getProperty("ORG_ROLE_MAPPING_SERVICE_POSTGRES_PASS"));
        System.out.println(" Port is : " + env.getProperty("ORG_ROLE_MAPPING_SERVICE_POSTGRES_PORT"));
        System.out.println("End printing env variables");
    }

    @Bean
    public ServiceAuthorisationApi generateServiceAuthorisationApi(@Value("${idam.s2s-auth.url}") final String s2sUrl) {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .contract(new SpringMvcContract())
                .target(ServiceAuthorisationApi.class, s2sUrl);
    }

    @Bean
    public ServiceAuthTokenGenerator authTokenGenerator(
            @Value("${idam.s2s-auth.totp_secret}") final String secret,
            @Value("${idam.s2s-auth.microservice}") final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi) {
        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }
}
