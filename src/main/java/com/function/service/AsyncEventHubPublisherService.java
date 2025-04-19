package com.function.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.function.util.RetryUtils;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.microsoft.azure.functions.ExecutionContext;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous implementation of EventHubPublisherService.
 * This service is responsible for publishing events to Azure Event Hubs asynchronously.
 * It uses retry logic to handle transient failures during publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncEventHubPublisherService implements EventHubPublisherService {

    // Azure Event Hub producer client for sending events asynchronously
    private final EventHubProducerAsyncClient producerAsyncClient;
    private final CloudEventSerializer cloudEventSerializer;
    private final String cloudEventTopic;

    /**
     * Synchronous publishing is not supported in this service.
     * Use SyncEventHubPublisherService for synchronous publishing.
     *
     * @param event   The CloudEvent to be published.
     * @param context The execution context for logging.
     * @throws UnsupportedOperationException Always thrown to indicate unsupported operation.
     */
    @Override
    public void publishSync(CloudEvent event, ExecutionContext context) {
        throw new UnsupportedOperationException("Use SyncEventHubPublisherService for sync publishing.");
    }

    /**
     * Publishes a CloudEvent to Azure Event Hubs asynchronously.
     * This method uses retry logic to handle transient failures.
     *
     * @param event   The CloudEvent to be published.
     * @param context The execution context for logging.
     * @return A CompletableFuture that completes when the event is successfully published.
     */
    @Override
    public CompletableFuture<Void> publishAsync(CloudEvent event, ExecutionContext context) {
        return RetryUtils.retryAsync(() -> {
            // Serialize the CloudEvent into a byte array
            byte[] serialized = serializeEvent(event, context);

            // Wrap the serialized data into an EventData object
            EventData eventData = new EventData(serialized);

            // Send the event data to Event Hubs asynchronously
            return producerAsyncClient.send(Collections.singletonList(eventData))
                .doOnSuccess(aVoid -> context.getLogger().info("Async publish succeeded: " + event.getId()))
                .doOnError(error -> context.getLogger().severe("Async publish failed: " + error.getMessage()))
                .toFuture();
        }, context);
    }

    /**
     * Serializes a CloudEvent into a byte array using CloudEventSerializer.
     *
     * @param event   The CloudEvent to be serialized.
     * @param context The execution context for logging.
     * @return A byte array representing the serialized CloudEvent.
     * @throws RuntimeException If serialization fails.
     */
    private byte[] serializeEvent(CloudEvent event, ExecutionContext context) {
        try {
            // Use the injected CloudEventSerializer for serialization
            return cloudEventSerializer.serialize(cloudEventTopic, event);
        } catch (Exception e) {
            // Log the serialization failure and throw a RuntimeException
            context.getLogger().severe("Failed to serialize CloudEvent: " + e.getMessage());
            throw new RuntimeException("Serialization failed", e);
        }
    }
}

