package demo._part13_completable_future.part3;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

// apply Function after finishing future
public class ThenApply extends Demo1 {

    @Test
    public void testThenApply() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"))
                .thenApply(String::toUpperCase);
        assertEquals("VALUE", future.get());
    }
}
