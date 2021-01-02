package demo.part13_completable_future.part10;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OrTimeout extends Demo1 {

    @Test
    public void getNow() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
                .orTimeout(3, TimeUnit.SECONDS);
        assertEquals("value", future.get());
    }

    @Test
    public void getNowValueIfAbsent() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
                .orTimeout(1, TimeUnit.SECONDS);
        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            assertEquals(TimeoutException.class, e.getCause().getClass());
        }
    }
}
