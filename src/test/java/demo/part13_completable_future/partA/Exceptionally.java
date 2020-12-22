package demo.part13_completable_future.partA;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class Exceptionally extends Demo1 {

    @Test
    public void testExceptionallySuccess() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value")
                .exceptionally(t -> "error: " + t.getMessage());
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("value", future.get());
    }

    @Test
    public void testExceptionallyError() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.<String>failedFuture(new RuntimeException("error"))
                .exceptionally(t -> "error: " + t.getMessage());
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("error: error", future.get());
    }
}
