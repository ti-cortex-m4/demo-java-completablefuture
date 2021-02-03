package demo.part13_completable_future.part1;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class ThenApply extends Demo1 {

    @Test
    public void testThenApply() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("single"))
                .thenApply(s -> "applied: " + s);
        assertEquals("applied: single", future.get());
    }
}
