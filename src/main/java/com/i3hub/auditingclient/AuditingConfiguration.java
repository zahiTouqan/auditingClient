package com.i3hub.auditingclient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@ConditionalOnMissingBean(name = "jpaAuditingHandler")
@EnableJpaAuditing(auditorAwareRef="auditorProvider")
@ConfigurationProperties(prefix = "auditing")
public class AuditingConfiguration {
    private boolean rest = true; // true: REST Auditing, false: kafka
    private String restURL;

    private String topic;
    private String serviceName;

    @Bean
    AuditorAware<Long> auditorProvider() {
        return new AuditorAwareImpl();
    }
}
