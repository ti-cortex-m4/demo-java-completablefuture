package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class Example3 extends Demo1 {

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
}
