package demo.part13_completable_future.part3;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

// run Runnable after finishing any future
public class RunAfterEither extends Demo1 {

    @Test
    public void testRunAfterEither() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .runAfterEither(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        () -> logger.info("finished one"));
        assertNull(future.get());
    }
}
