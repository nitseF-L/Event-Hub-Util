package com.function.config;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.function.util.AzureCredentialProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires native Azure SDK Event Hub clients using values from application.yml.
 */
@Configuration
public class EventHubClientConfig {

    @Value("${spring.cloud.azure.eventhub.namespace}")
    private String fullyQualifiedNamespace;

    @Value("${spring.cloud.azure.eventhub.name}")
    private String eventHubName;

    @Value("${spring.cloud.azure.credential.client-id}")
    private String clientId;

    @Value("${spring.cloud.azure.credential.client-secret}")
    private String clientSecret;

    @Value("${spring.cloud.azure.profile.tenant-id}")
    private String tenantId;

    @Value("${spring.cloud.azure.credential.managed-identity-enabled:false}")
    private boolean useManagedIdentity;

    @Bean
    public EventHubProducerClient eventHubProducerClient() {
        return new EventHubClientBuilder()
            .fullyQualifiedNamespace(fullyQualifiedNamespace)
            .credential(AzureCredentialProvider.get(tenantId, clientId, clientSecret, useManagedIdentity))
            .buildProducerClient();
    }

    @Bean
    public EventHubProducerAsyncClient eventHubProducerAsyncClient() {
        return new EventHubClientBuilder()
            .fullyQualifiedNamespace(fullyQualifiedNamespace)
            .credential(AzureCredentialProvider.get(tenantId, clientId, clientSecret, useManagedIdentity))
            .buildAsyncProducerClient();
    }
}

