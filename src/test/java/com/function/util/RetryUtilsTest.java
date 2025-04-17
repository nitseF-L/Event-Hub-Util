package com.function.util;

import com.microsoft.azure.functions.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class RetryUtilsTest {

    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        context = mock(ExecutionContext.class);
        when(context.getLogger()).thenReturn(Logger.getLogger("TestLogger"));
    }

    @Test
    void retry_shouldSucceedAfterRetries() {
        AtomicInteger counter = new AtomicInteger(0);
        String result = RetryUtils.retry(() -> {
            if (counter.incrementAndGet() < 3) throw new RuntimeException("fail");
            return "success";
        }, context);

        assertThat(result).isEqualTo("success");
        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    void retry_shouldFailAfterMaxRetries() {
        AtomicInteger counter = new AtomicInteger(0);

        assertThatThrownBy(() -> RetryUtils.retry(() -> {
            counter.incrementAndGet();
            throw new RuntimeException("fail");
        }, context)).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Operation failed after retries");

        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    void retryAsync_shouldSucceedAfterRetries() {
        AtomicInteger counter = new AtomicInteger(0);

        CompletableFuture<Void> future = RetryUtils.retryAsync(() -> {
            if (counter.incrementAndGet() < 3) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(new RuntimeException("fail"));
                return failed;
            }
            return CompletableFuture.completedFuture(null);
        }, context);

        assertThatCode(future::join).doesNotThrowAnyException();
        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    void retryAsync_shouldFailAfterMaxRetries() {
        AtomicInteger counter = new AtomicInteger(0);

        CompletableFuture<Void> future = RetryUtils.retryAsync(() -> {
            counter.incrementAndGet();
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("fail"));
            return failed;
        }, context);

        assertThatThrownBy(future::join)
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("fail");

        assertThat(counter.get()).isEqualTo(3);
    }
} 

