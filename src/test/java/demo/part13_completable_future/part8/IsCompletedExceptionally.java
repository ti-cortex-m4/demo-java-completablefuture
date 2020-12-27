package demo.part13_completable_future.part8;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IsCompletedExceptionally extends Demo1 {

    @Test
    public void testIsCompletedExceptionallyFalse() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");
        assertFalse(future.isCompletedExceptionally());
        assertEquals("value", future.get());
    }

    @Test
    public void testIsCompletedExceptionallyTrue() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("error"));
        assertTrue(future.isCompletedExceptionally());
    }
}