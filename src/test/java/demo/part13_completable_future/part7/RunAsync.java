package demo.part13_completable_future.part7;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

// The runAsync methods are useful if you don’t need to return anything
public class RunAsync extends Demo1 {

    @Test
    public void testRunAsync() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> logger.info(sleepAndGet("value")));
        assertNull(future.get());
    }
}
