package demo.completable_future.part7;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.junit.Assert.assertNull;

public class AnyOf extends Demo1 {

    @Test
    public void testAnyOf() throws InterruptedException, ExecutionException {
        CompletableFuture<Object> future = CompletableFuture.anyOf(
                supplyAsync(() -> sleepAndGet(1, "parallel1")),
                supplyAsync(() -> sleepAndGet(2, "parallel2")),
                supplyAsync(() -> sleepAndGet(3, "parallel3"))
        );

        assertEquals("parallel1", future.get());
    }

    @Test
    public void testApplyToEither() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
        CompletableFuture<String> future2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

        CompletableFuture<String> future = future1
                .applyToEither(future2, value -> value);

        assertEquals("parallel1", future.get());
    }
}
