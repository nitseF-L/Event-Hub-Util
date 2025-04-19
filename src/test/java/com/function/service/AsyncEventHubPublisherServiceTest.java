package com.function.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.microsoft.azure.functions.ExecutionContext;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.kafka.CloudEventSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AsyncEventHubPublisherServiceTest {

    private EventHubProducerAsyncClient producerAsyncClient;
    private CloudEventSerializer cloudEventSerializer;
    private ExecutionContext context;
    private AsyncEventHubPublisherService service;
    private String cloudEventTopic; 

    @BeforeEach
    void setUp() {
        producerAsyncClient = mock(EventHubProducerAsyncClient.class);
        cloudEventSerializer = mock(CloudEventSerializer.class);
        context = mock(ExecutionContext.class);
        when(context.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        service = new AsyncEventHubPublisherService(producerAsyncClient, cloudEventSerializer, cloudEventTopic);
        cloudEventTopic = "deposit";
    }

    @Test
    void publishSync_shouldThrowUnsupportedOperationException() {
        CloudEvent event = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("test.type")
                .withSource(URI.create("/test"))
                .build();

        assertThatThrownBy(() -> service.publishSync(event, context))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Use SyncEventHubPublisherService for sync publishing");
    }

    @Test
    void publishAsync_shouldSendSuccessfully() {
        CloudEvent event = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("test.type")
                .withSource(URI.create("/test"))
                .build();

        // Mock the serialization process
        byte[] eventBytes = "mocked-event-bytes".getBytes(); // Mocked serialized bytes
        when(cloudEventSerializer.serialize(cloudEventTopic, event)).thenReturn(eventBytes);

        EventData expectedEventData = new EventData(eventBytes);

        // Mock the producerAsyncClient behavior
        when(producerAsyncClient.send(Collections.singletonList(expectedEventData))).thenReturn(Mono.empty());

        // Call the publishAsync method
        assertThatCode(() -> service.publishAsync(event, context).join())
                .doesNotThrowAnyException();

        // Verify that the producerAsyncClient's send method was called
        verify(producerAsyncClient, times(1)).send(Collections.singletonList(expectedEventData));
    }

    @Test
    void publishAsync_shouldLogAndThrowOnFailure() {
        CloudEvent event = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("test.type")
                .withSource(URI.create("/test"))
                .build();

        // Mock the serialization process
        byte[] eventBytes = "mocked-event-bytes".getBytes(); // Mocked serialized bytes
        when(cloudEventSerializer.serialize(cloudEventTopic, event)).thenReturn(eventBytes);

        EventData expectedEventData = new EventData(eventBytes);

        // Mock the producerAsyncClient behavior to simulate a failure
        when(producerAsyncClient.send(Collections.singletonList(expectedEventData)))
                .thenReturn(Mono.error(new RuntimeException("Simulated failure")));

        // Call the publishAsync method and verify it throws an exception
        assertThatThrownBy(() -> service.publishAsync(event, context).join())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated failure");

        // Verify that the producerAsyncClient's send method was called the expected number of times
        verify(producerAsyncClient, times(3)).send(Collections.singletonList(expectedEventData));
    }

    @Test
    void publishAsync_shouldThrowSerializationException() {
        CloudEvent event = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("test.type")
                .withSource(URI.create("/test"))
                .build();

        // Mock the serialization process to throw an exception
        when(cloudEventSerializer.serialize(cloudEventTopic, event))
                .thenThrow(new RuntimeException("Serialization failed"));

        // Call the publishAsync method and verify it throws an exception
        assertThatThrownBy(() -> service.publishAsync(event, context).join())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Serialization failed");

        // Verify that the producerAsyncClient's send method was never called
        verify(producerAsyncClient, never()).send(anyList());
    }
}