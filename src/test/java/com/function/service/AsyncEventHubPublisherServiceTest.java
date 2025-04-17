package com.function.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.microsoft.azure.functions.ExecutionContext;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsyncEventHubPublisherServiceTest {

    private EventHubProducerAsyncClient producerAsyncClient;
    private ExecutionContext context;
    private AsyncEventHubPublisherService service;

    @BeforeEach
    void setUp() {
        producerAsyncClient = mock(EventHubProducerAsyncClient.class);
        context = mock(ExecutionContext.class);
        when(context.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        service = new AsyncEventHubPublisherService(producerAsyncClient);
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
        EventData expectedEventData = new EventData(eventBytes);

        // Mock the producerAsyncClient behavior
        when(producerAsyncClient.send(Collections.singletonList(expectedEventData))).thenReturn(Mono.empty());

        // Mock the serialization logic in the service
        AsyncEventHubPublisherService serviceWithMockedSerialization = spy(service);
        doReturn(eventBytes).when(serviceWithMockedSerialization).serializeEvent(event, context);

        assertThatCode(() -> serviceWithMockedSerialization.publishAsync(event, context).join())
                .doesNotThrowAnyException();

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
        EventData expectedEventData = new EventData(eventBytes);

        // Mock the producerAsyncClient behavior to simulate a failure
        when(producerAsyncClient.send(Collections.singletonList(expectedEventData)))
                .thenReturn(Mono.error(new RuntimeException("Simulated failure")));

        // Mock the serialization logic in the service
        AsyncEventHubPublisherService serviceWithMockedSerialization = spy(service);
        doReturn(eventBytes).when(serviceWithMockedSerialization).serializeEvent(event, context);

        assertThatThrownBy(() -> serviceWithMockedSerialization.publishAsync(event, context).join())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated failure");

        // Adjust the verification to match the retry count
        verify(producerAsyncClient, times(3)).send(Collections.singletonList(expectedEventData));
    }
}