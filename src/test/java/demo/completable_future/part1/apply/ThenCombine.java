package demo.completable_future.part1.apply;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.junit.Assert.assertEquals;

public class ThenCombine extends Demo1 {

    @Test
    public void testThenCombine() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("parallel1"));
        CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet("parallel2"));

        CompletionStage<String> stage = stage1.thenCombine(stage2,
                (s1, s2) -> (s1 + " " + s2).toUpperCase());

        assertEquals("PARALLEL1 PARALLEL2", stage.toCompletableFuture().get());
    }
}