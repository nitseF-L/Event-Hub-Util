package com.function.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;

import com.microsoft.azure.functions.ExecutionContext;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.provider.EventFormatProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncEventHubPublisherServiceTest {

    private EventHubProducerClient producerClient;
    private ExecutionContext context;
    private SyncEventHubPublisherService service;

    @BeforeEach
    void setUp() {
        producerClient = mock(EventHubProducerClient.class);
        context = mock(ExecutionContext.class);
        when(context.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        service = new SyncEventHubPublisherService(producerClient);
    }

    @Test
    void publishSync_shouldSendSuccessfully() {
        CloudEvent event = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("test.type")
                .withSource(URI.create("/test"))
                .build();

        // Resolve the CloudEvent format
        var eventFormat = EventFormatProvider.getInstance().resolveFormat("application/cloudevents+json");
        assertThat(eventFormat).isNotNull(); // Ensure the format is not null

        // Serialize the CloudEvent into a byte array
        byte[] eventBytes = eventFormat.serialize(event);

        EventData expectedEventData = new EventData(eventBytes);

        // Call the publishSync method and verify no exceptions are thrown
        assertThatCode(() -> service.publishSync(event, context))
                .doesNotThrowAnyException();

        // Verify that the producerClient's send method was called with the expected data        
        verify(producerClient, times(1)).send(Collections.singletonList(expectedEventData));
    }

    @Test
    void publishAsync_shouldThrowUnsupportedOperation() {
        CloudEvent event = mock(CloudEvent.class);

        assertThatThrownBy(() -> service.publishAsync(event, context))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Use AsyncEventHubPublisherService");
    }
}
