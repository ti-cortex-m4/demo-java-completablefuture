package demo._part13_completable_future.part1;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

// complete future exceptionally, cause the Throwable
public class CompleteExceptionally extends Demo1 {

    @Test
    public void testCompleteExceptionally() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = new CompletableFuture<>();
        assertFalse(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        future.completeExceptionally(new RuntimeException("error"));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(true);
        }
    }
}
