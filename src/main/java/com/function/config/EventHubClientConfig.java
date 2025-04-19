package com.function.config;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import io.cloudevents.kafka.CloudEventSerializer;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up Azure Event Hub producer clients
 * and shared dependencies such as credential resolution and serialization.
 */
@Configuration
@AllArgsConstructor
public class EventHubClientConfig {

    // Injects EventHub configuration properties defined in application.yml
    private final EventHubProperties properties;

    /**
     * Determines which Azure credential to use based on the configuration.
     * If useManagedIdentity is true, DefaultAzureCredential will be used (for MSI).
     * Otherwise, a service principal credential will be constructed.
     *
     * @return TokenCredential for authenticating with Azure Event Hubs.
     */
    @Bean
    public TokenCredential tokenCredential() {
        return properties.isUseManagedIdentity()
            ? new DefaultAzureCredentialBuilder().build()
            : new ClientSecretCredentialBuilder()
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .tenantId(properties.getTenantId())
                .build();
    }

    /**
     * Creates a synchronous EventHubProducerClient using resolved credentials and configuration.
     *
     * @param tokenCredential the credential used to authenticate the client
     * @return EventHubProducerClient instance
     */
    @Bean
    public EventHubProducerClient eventHubProducerClient(TokenCredential tokenCredential) {
        return new EventHubClientBuilder()
            .fullyQualifiedNamespace(properties.getNamespace())
            .eventHubName(properties.getEventHubName())
            .credential(tokenCredential)
            .buildProducerClient();
    }

    /**
     * Creates an asynchronous EventHubProducerAsyncClient using resolved credentials and configuration.
     *
     * @param tokenCredential the credential used to authenticate the client
     * @return EventHubProducerAsyncClient instance
     */
    @Bean
    public EventHubProducerAsyncClient eventHubProducerAsyncClient(TokenCredential tokenCredential) {
        return new EventHubClientBuilder()
            .fullyQualifiedNamespace(properties.getNamespace())
            .eventHubName(properties.getEventHubName())
            .credential(tokenCredential)
            .buildAsyncProducerClient();
    }

    /**
     * Provides a reusable CloudEventSerializer for serializing CloudEvents before publishing.
     * This serializer is used by both sync and async publishers.
     *
     * @return CloudEventSerializer instance
     */
    @Bean
    public CloudEventSerializer cloudEventSerializer() {
        return new CloudEventSerializer();
    }
}