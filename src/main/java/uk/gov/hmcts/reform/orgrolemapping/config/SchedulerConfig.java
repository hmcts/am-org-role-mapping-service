package uk.gov.hmcts.reform.orgrolemapping.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnExpression(
        "${professional.role.mapping.scheduling.enabled} || ${idam.role.management.scheduling.enabled}")
public class SchedulerConfig {
}
