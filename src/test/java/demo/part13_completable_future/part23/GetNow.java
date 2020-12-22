package demo.part13_completable_future.part23;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class GetNow extends Demo1 {

    @Test
    public void getNow() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");
        assertEquals("value", future.getNow("value2"));
    }

    @Test
    public void getNowValueIfAbsent() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
        assertEquals("value2", future.getNow("value2"));
    }
}
