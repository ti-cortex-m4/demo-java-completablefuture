package demo.part13_completable_future.part2;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class ThenCompose2 extends Demo1 {

    @Test
    public void testThenApply() throws Exception {
        int x = 2;
        int y = 3;
        CompletableFuture<CompletableFuture<Integer>> completableFuture = CompletableFuture.supplyAsync(() -> x)
                .thenApply(n -> CompletableFuture.supplyAsync(() -> n + y));
        assertEquals(5, completableFuture.get().get().intValue());
    }

    @Test
    public void testThenCompose() throws Exception {
        int x = 2;
        int y = 3;
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> x)
                .thenCompose(n -> CompletableFuture.supplyAsync(() -> n + y));
        assertEquals(5, completableFuture.get().intValue());
    }

    @Test
    public void testThenCombine() throws Exception {
        int x = 2;
        int y = 3;
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> x)
                .thenCombine(CompletableFuture.supplyAsync(() -> y), (n1, n2) -> n1 + n2);
        assertEquals(5, completableFuture.get().intValue());
    }
}
