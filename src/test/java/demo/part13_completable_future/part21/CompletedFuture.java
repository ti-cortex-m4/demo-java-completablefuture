package demo.part13_completable_future.part21;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class CompletedFuture extends Demo1 {

    @Test
    public void testCompletedFuture() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("value", future.get());
    }

    @Test
    public void testCompletedStage() throws InterruptedException, ExecutionException {
        CompletionStage<String> future = CompletableFuture.completedStage("value");
        assertTrue(future.toCompletableFuture().isDone());
        assertFalse(future.toCompletableFuture().isCompletedExceptionally());
        assertEquals("value", future.toCompletableFuture().get());
    }
}

