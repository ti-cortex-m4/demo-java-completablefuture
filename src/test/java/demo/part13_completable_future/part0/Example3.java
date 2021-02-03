package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Example3 extends Demo1 {

    @Test
    public void testThenApply() throws Exception {
        CompletableFuture<CompletableFuture<Integer>> future1 = supplyAsync(() -> 2)
                .thenApply(n -> supplyAsync(() -> n + 3));
        CompletableFuture<Integer> future2 = future1.get();
        assertEquals(5, future2.get().intValue());
    }

    @Test
    public void testThenCompose() throws Exception {
        CompletableFuture<Integer> future = supplyAsync(() -> 2)
                .thenCompose(n -> supplyAsync(() -> n + 3));
        assertEquals(5, future.get().intValue());
    }
}
