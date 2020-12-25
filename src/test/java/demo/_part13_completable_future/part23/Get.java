package demo._part13_completable_future.part23;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

// The get method declares an InterruptedException, ExecutionException, which is a checked exception
// The join method is the same as get except that it throws an unchecked CompletionException if completed exceptionally, again with the underlying exception as its cause.
public class Get extends Demo1 {

    @Test
    public void testGet() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"));
        assertEquals("value", future.get());
    }

    @Test
    public void testGetWithTimeoutSuccess() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"));
        assertEquals("value", future.get(3, TimeUnit.SECONDS));
    }

    @Test
    public void testGetWithTimeoutFailure() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"));
        try {
            future.get(1, TimeUnit.SECONDS);
            fail();
        } catch (TimeoutException e) {
            assertTrue(true);
            //assertEquals(TimeoutException.class, e.getCause().getClass());
        }
    }
}
