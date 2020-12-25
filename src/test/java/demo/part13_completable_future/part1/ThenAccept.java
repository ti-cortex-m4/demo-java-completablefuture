package demo.part13_completable_future.part1;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

// accept result in Consumer after finishing future
public class ThenAccept extends Demo1 {

    @Test
    public void testThenAccept() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"))
                .thenAccept(s -> logger.info("consumed: " + s));
        assertNull(future.get());
    }
}
