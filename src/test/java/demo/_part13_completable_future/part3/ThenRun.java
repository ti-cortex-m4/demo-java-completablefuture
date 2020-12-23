package demo._part13_completable_future.part3;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

// run Runnable after finishing future
public class ThenRun extends Demo1 {

    @Test
    public void testThenRun() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"))
                .thenRun(() -> logger.info("run in Runnable"));
        assertNull(future.get());
    }
}
