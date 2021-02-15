package demo.part13_completable_future.part13;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static java.util.concurrent.CompletableFuture.supplyAsync;

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
}
