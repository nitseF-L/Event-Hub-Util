package com.function.util;

import com.microsoft.azure.functions.ExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility class for retrying synchronous and asynchronous operations.
 */
public class RetryUtils {

    private static final int MAX_RETRIES = 3;

    /**
     * Retries a synchronous operation up to MAX_RETRIES.
     */
    public static <T> T retry(Supplier<T> supplier, ExecutionContext context) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return supplier.get();
            } catch (Exception e) {
                attempt++;
                context.getLogger().warning("Retry " + attempt + " failed: " + e.getMessage());
                if (attempt >= MAX_RETRIES) {
                    context.getLogger().severe("Operation failed after retries: " + e.getMessage());
                    throw new RuntimeException("Operation failed after retries", e);
                }
            }
        }
        return null;
    }

    /**
     * Retries an asynchronous operation up to MAX_RETRIES.
     */
    public static CompletableFuture<Void> retryAsync(Supplier<CompletableFuture<Void>> operation, ExecutionContext context) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        retryAsyncInternal(operation, context, 0, result);
        return result;
    }

    private static void retryAsyncInternal(
        Supplier<CompletableFuture<Void>> operation,
        ExecutionContext context,
        int attempt,
        CompletableFuture<Void> result
    ) {
        operation.get().whenComplete((res, ex) -> {
            if (ex == null) {
                result.complete(res);
            } else {
                int nextAttempt = attempt + 1;
                context.getLogger().warning("Async retry " + nextAttempt + " failed: " + ex.getMessage());
                if (nextAttempt >= MAX_RETRIES) {
                    context.getLogger().severe("Async operation failed after retries: " + ex.getMessage());
                    result.completeExceptionally(ex);
                } else {
                    retryAsyncInternal(operation, context, nextAttempt, result);
                }
            }
        });
    }

    // public static void retry(Supplier<T> supplier, ExecutionContext context) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'retry'");
    // }
}

