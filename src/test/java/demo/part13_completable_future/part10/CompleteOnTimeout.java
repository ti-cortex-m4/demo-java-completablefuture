package demo.part13_completable_future.part10;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class CompleteOnTimeout extends Demo1 {

    @Test
    public void testCompleteOnTimeout1() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
                .completeOnTimeout("value2", 3, TimeUnit.SECONDS);
        assertEquals("value", future.get());
    }

    @Test
    public void testCompleteOnTimeout2() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
                .completeOnTimeout("value2", 1, TimeUnit.SECONDS);
        assertEquals("value2", future.get());
    }
}
