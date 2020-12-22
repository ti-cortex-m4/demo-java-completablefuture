package demo.part13_completable_future.part24;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class OrTimeout extends Demo1 {

    @Test
    public void getNow() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
                .orTimeout(1, TimeUnit.SECONDS);
        assertEquals("value", future.get());
    }

    @Test
    public void getNowValueIfAbsent() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
                .orTimeout(3, TimeUnit.SECONDS);
        assertEquals("value", future.get());
    }
}
