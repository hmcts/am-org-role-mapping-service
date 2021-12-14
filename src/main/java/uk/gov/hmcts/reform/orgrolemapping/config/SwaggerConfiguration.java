package uk.gov.hmcts.reform.orgrolemapping.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.Arrays;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfiguration {


    private static final String VALUE = "string";
    private static final String HEADER = "header";

    @Bean
    public Docket apiV2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("v2")
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .build()
                .useDefaultResponseMessages(false)
                .apiInfo(apiV2Info())
                .globalOperationParameters(Arrays.asList(
                        headerServiceAuthorization(),
                        headerAuthorization()
                ));
    }

    private ApiInfo apiV2Info() {
        return new ApiInfoBuilder()
                .title("Organisation Role Mapping Service")
                .description("Organisation Role Mapping Service")
                .version("2")
                .build();
    }

    private Parameter headerServiceAuthorization() {
        return new ParameterBuilder()
                .name("ServiceAuthorization")
                .description("Valid Service-to-Service JWT token for a whitelisted micro-service")
                .modelRef(new ModelRef(VALUE))
                .parameterType(HEADER)
                .required(true)
                .build();
    }

    private Parameter headerAuthorization() {
        return new ParameterBuilder()
                .name("Authorization")
                .description("Keyword `Bearer` followed by a valid IDAM user token")
                .modelRef(new ModelRef(VALUE))
                .parameterType(HEADER)
                .required(true)
                .build();
    }
}
