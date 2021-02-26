package demo.completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class Example2 extends Demo1 {

    // area = π * r^2
    @Test
    public void test() {
        CompletableFuture<Double> pi = CompletableFuture.supplyAsync(() -> Math.PI);
        CompletableFuture<Integer> radius = CompletableFuture.supplyAsync(() -> 1);

        CompletableFuture<Void> result = radius
                .thenApply(r -> r * r)
                .thenCombine(pi, (aDouble, integer) -> aDouble * integer)
                .thenAccept(area -> logger.info("area: {}", area))
                .thenRun(() -> logger.info("completed"));

        result.join();
    }
}
