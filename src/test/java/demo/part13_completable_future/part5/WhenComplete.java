package demo.part13_completable_future.part5;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class WhenComplete extends Demo1 {

    @Test
    public void testWhenCompleteSuccess() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value")
                .whenComplete((value, t) -> {
                    if (t == null) {
                        logger.info("success: " + value);
                    } else {
                        logger.info("error: " + t.getMessage());
                    }
                });
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("value", future.get());
    }

    @Test
    public void testWhenCompleteError() {
        CompletableFuture<String> future = CompletableFuture.<String>failedFuture(new RuntimeException("error"))
                .whenComplete((value, t) -> {
                    if (t == null) {
                        logger.info("success: " + value);
                    } else {
                        logger.info("error: " + t.getMessage());
                    }
                });
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
    }
}
