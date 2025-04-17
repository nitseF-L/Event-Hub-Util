package com.function.util;

import com.microsoft.azure.functions.ExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility class for retrying synchronous and asynchronous operations.
 * This class provides methods to retry operations up to a maximum number of attempts
 * in case of transient failures.
 */
public class RetryUtils {

    // Maximum number of retry attempts
    private static final int MAX_RETRIES = 3;

    /**
     * Retries a synchronous operation up to MAX_RETRIES times.
     *
     * @param supplier The operation to be retried, represented as a Supplier.
     * @param context  The execution context for logging.
     * @param <T>      The return type of the operation.
     * @return The result of the operation if it succeeds within the retry limit.
     * @throws RuntimeException If the operation fails after the maximum number of retries.
     */
    public static <T> T retry(Supplier<T> supplier, ExecutionContext context) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                // Attempt to execute the operation
                return supplier.get();
            } catch (Exception e) {
                attempt++;
                // Log the failure and retry if the maximum attempts are not reached
                context.getLogger().warning("Retry " + attempt + " failed: " + e.getMessage());
                if (attempt >= MAX_RETRIES) {
                    // Log the final failure and throw an exception
                    context.getLogger().severe("Operation failed after retries: " + e.getMessage());
                    throw new RuntimeException("Operation failed after retries", e);
                }
            }
        }
        return null; // This line is unreachable but required for compilation
    }

    /**
     * Retries an asynchronous operation up to MAX_RETRIES times.
     *
     * @param operation The asynchronous operation to be retried, represented as a Supplier of CompletableFuture.
     * @param context   The execution context for logging.
     * @return A CompletableFuture that completes when the operation succeeds or fails after retries.
     */
    public static CompletableFuture<Void> retryAsync(Supplier<CompletableFuture<Void>> operation, ExecutionContext context) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        // Start the retry process
        retryAsyncInternal(operation, context, 0, result);
        return result;
    }

    /**
     * Internal method to handle asynchronous retries.
     *
     * @param operation The asynchronous operation to be retried.
     * @param context   The execution context for logging.
     * @param attempt   The current retry attempt.
     * @param result    The CompletableFuture to complete with the operation's result or exception.
     */
    private static void retryAsyncInternal(
        Supplier<CompletableFuture<Void>> operation,
        ExecutionContext context,
        int attempt,
        CompletableFuture<Void> result
    ) {
        // Execute the asynchronous operation
        operation.get().whenComplete((res, ex) -> {
            if (ex == null) {
                // Complete successfully if no exception occurred
                result.complete(res);
            } else {
                int nextAttempt = attempt + 1;
                // Log the failure and retry if the maximum attempts are not reached
                context.getLogger().warning("Async retry " + nextAttempt + " failed: " + ex.getMessage());
                if (nextAttempt >= MAX_RETRIES) {
                    // Log the final failure and complete exceptionally
                    context.getLogger().severe("Async operation failed after retries: " + ex.getMessage());
                    result.completeExceptionally(ex);
                } else {
                    // Retry the operation
                    retryAsyncInternal(operation, context, nextAttempt, result);
                }
            }
        });
    }
}