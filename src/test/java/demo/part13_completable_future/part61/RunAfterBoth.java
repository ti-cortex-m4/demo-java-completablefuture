package demo.part13_completable_future.part61;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

// run Runnable after finishing both futures
public class RunAfterBoth extends Demo1 {

    @Test
    public void testRunAfterBoth() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .runAfterBoth(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        () -> logger.info("finished both"));
        assertNull(future.get());
    }
}

