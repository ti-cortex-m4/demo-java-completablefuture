package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Example3 extends Demo1 {

    @Test
    public void testThenApplyFast() throws Exception {
        CompletableFuture<Integer> future = supplyAsync(() -> 2)
                .thenApply(i -> i + 3); // Function<Integer, Integer>
        assertEquals(5, future.get().intValue());
    }

    @Test
    public void testThenApplySlow() throws Exception {
        CompletableFuture<CompletableFuture<Integer>> future1 = supplyAsync(() -> 2)
                .thenApply(i -> supplyAsync(() -> i + 3)); // Function<Integer, CompletableFuture<Integer>>
        CompletableFuture<Integer> future2 = future1.get(); // blocking
        assertEquals(5, future2.get().intValue());
    }

    @Test
    public void testThenCompose() throws Exception {
        CompletableFuture<Integer> future = supplyAsync(() -> 2)
                .thenCompose(i -> supplyAsync(() -> i + 3)); // Function<Integer, CompletionStage<Integer>>
        assertEquals(5, future.get().intValue());
    }
}
