package uk.gov.hmcts.reform.orgrolemapping.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

@Configuration
public class SwaggerConfiguration {

    private static final String DESCRIPTION = "API to provision various organisation roles for staff & judicial users"
            + " based on the service specific mapping rules.";

    @Bean
    public GroupedOpenApi publicApi(OperationCustomizer customGlobalHeaders) {
        return GroupedOpenApi.builder()
                .group("am-org-role-mapping-service")
                .pathsToMatch("/am/role-mapping/**")
                .build();
    }

    @ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
    @Bean
    GroupedOpenApi testingSupportApis(OperationCustomizer customGlobalHeaders) {
        return GroupedOpenApi.builder()
                .group("testing-support")
                .displayName("Testing Support")
                .pathsToMatch("/am/testing-support/**")
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                AUTHORIZATION,
                                new SecurityScheme()
                                        .name(AUTHORIZATION)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Valid IDAM user token, (Bearer keyword is "
                                                + "added automatically)")
                        )
                        .addSecuritySchemes(SERVICE_AUTHORIZATION,
                                new SecurityScheme()
                                        .in(SecurityScheme.In.HEADER)
                                        .name(SERVICE_AUTHORIZATION)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Valid Service-to-Service JWT token for a "
                                                + "whitelisted micro-service")
                        )
                )
                .info(new Info().title("AM Organisational Role Mapping Service")
                        .description(DESCRIPTION))
                .externalDocs(new ExternalDocumentation()
                        .description("README")
                        .url("https://github.com/hmcts/am-org-role-mapping-service#readme"))
                .addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION))
                .addSecurityItem(new SecurityRequirement().addList(SERVICE_AUTHORIZATION));
    }

    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (Operation customOperation, HandlerMethod handlerMethod) -> {
            Parameter serviceAuthorizationHeader = new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema())
                    .name(SERVICE_AUTHORIZATION)
                    .description("Valid Service-to-Service JWT token for a whitelisted micro-service")
                    .required(true);
            Parameter authorizationHeader = new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema())
                    .name(AUTHORIZATION)
                    .description("Keyword `Bearer` followed by a valid IDAM user token")
                    .required(true);
            customOperation.addParametersItem(authorizationHeader);
            customOperation.addParametersItem(serviceAuthorizationHeader);
            return customOperation;
        };
    }
}
