package demo.part13_completable_future.part9;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// The completeExceptionally method completes the CompletableFuture with a given exception.
public class CompleteExceptionally extends Demo1 {

    @Test
    public void testCompleteExceptionally() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = new CompletableFuture<>();
        assertFalse(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        future.completeExceptionally(new RuntimeException("exception"));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            assertEquals(RuntimeException.class, cause.getClass());
            assertEquals("exception", cause.getMessage());
        }
    }
}
