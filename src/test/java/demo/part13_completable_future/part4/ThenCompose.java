package demo.part13_completable_future.part4;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ThenCompose extends Demo1 {

    @Test
    public void testThenCompose() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("sequential1"))
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> sleepAndGet(s + " sequential2")));
        assertEquals("sequential1 sequential2", future.get());
    }
}
