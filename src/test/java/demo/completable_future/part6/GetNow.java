package demo.completable_future.part6;

import demo.common.Demo;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GetNow extends Demo {

    @Test
    public void getNow() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");
        assertEquals("value", future.getNow("value2"));
        assertTrue(future.isDone());
    }

    @Test
    public void getNowValueIfAbsent() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
        assertEquals("default", future.getNow("default"));
        assertFalse(future.isDone());
    }
}
