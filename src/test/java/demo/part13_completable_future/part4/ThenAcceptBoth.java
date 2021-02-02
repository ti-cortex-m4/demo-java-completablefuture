package demo.part13_completable_future.part4;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

public class ThenAcceptBoth extends Demo1 {

    @Test
    public void testThenAcceptBoth() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        (s1, s2) -> logger.info("consumed both: " + s1 + " " + s2));
        assertNull(future.get());
    }
}
