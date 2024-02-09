package uk.gov.hmcts.reform.orgrolemapping.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "professional.role.mapping.scheduling.enabled")
public class SchedulerConfig {
}