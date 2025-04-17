package com.function.service;

import io.cloudevents.CloudEvent;
import com.microsoft.azure.functions.ExecutionContext;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for publishing CloudEvents to Azure Event Hub.
 */
public interface EventHubPublisherService {

    void publishSync(CloudEvent event, ExecutionContext context);

    CompletableFuture<Void> publishAsync(CloudEvent event, ExecutionContext context);
}


