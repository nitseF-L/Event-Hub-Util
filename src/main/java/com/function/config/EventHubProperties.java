package com.function.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "eventhub")
public class EventHubProperties {

    private String namespace;
    private String eventHubName;
    private boolean useManagedIdentity;
    
    // Only required if not using MSI
    private String clientId;
    private String clientSecret;
    private String tenantId;
}