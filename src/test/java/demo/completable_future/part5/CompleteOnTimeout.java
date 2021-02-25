package demo.completable_future.part5;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class CompleteOnTimeout extends Demo1 {

    @Test
    public void testCompleteOnTimeout1() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "value"))
                .completeOnTimeout("default", 2, TimeUnit.SECONDS);
        assertEquals("value", future.get());
    }

    @Test
    public void testCompleteOnTimeout2() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
                .completeOnTimeout("default", 1, TimeUnit.SECONDS);
        assertEquals("default", future.get());
    }
}
