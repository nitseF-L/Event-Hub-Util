package com.function.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.function.util.RetryUtils;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.microsoft.azure.functions.ExecutionContext;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Synchronous implementation of EventHubPublisherService.
 * This service is responsible for publishing events to Azure Event Hubs synchronously.
 * It uses retry logic to handle transient failures during publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncEventHubPublisherService implements EventHubPublisherService {

    // Azure Event Hub producer client for sending events synchronously
    private final EventHubProducerClient producerClient;
    private final String cloudEventTopic;

    /**
     * Publishes a CloudEvent to Azure Event Hubs synchronously.
     * This method uses retry logic to handle transient failures.
     *
     * @param event   The CloudEvent to be published.
     * @param context The execution context for logging.
     * @throws RuntimeException If the event format is unsupported or publishing fails.
     */
    public void publishSync(CloudEvent event, ExecutionContext context) {
        RetryUtils.retry(() -> {
            // Resolve the CloudEvent format for serialization
            EventFormat eventFormat = EventFormatProvider
                .getInstance()
                .resolveFormat("application/cloudevents+json");

            // Throw an exception if the required format is not supported
            if (eventFormat == null) {
                throw new RuntimeException("Event format 'application/cloudevents+json' is not supported");
            }

            // Serialize the CloudEvent into a byte array
            byte[] eventBytes = eventFormat.serialize(event);

            // Wrap the serialized data into an EventData object and send it synchronously
            producerClient.send(Collections.singletonList(new EventData(eventBytes)));

            // Return null to satisfy the lambda's return type
            return null;
        }, context);
    }

    /**
     * Asynchronous publishing is not supported in this service.
     * Use AsyncEventHubPublisherService for asynchronous publishing.
     *
     * @param event   The CloudEvent to be published.
     * @param context The execution context for logging.
     * @throws UnsupportedOperationException Always thrown to indicate unsupported operation.
     */
    @Override
    public CompletableFuture<Void> publishAsync(CloudEvent event, ExecutionContext context) {
        throw new UnsupportedOperationException("Use AsyncEventHubPublisherService for async publishing.");
    }
}
