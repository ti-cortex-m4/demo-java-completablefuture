package demo._part13_completable_future.part22;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class IsDone extends Demo1 {

    @Test
    public void testIsDoneTrue() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");
        assertTrue(future.isDone());
        assertEquals("value", future.get());
    }

    @Test
    public void testIsDoneFalse() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
        assertFalse(future.isDone());
    }
}