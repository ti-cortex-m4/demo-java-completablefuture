package demo.completable_future.part1.apply;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.junit.Assert.assertEquals;

public class ThenApply extends Demo1 {

    @Test
    public void testThenApply() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("single"));

        CompletionStage<String> stage = stage1.thenApply(
                s -> s.toUpperCase());

        assertEquals("SINGLE", stage.toCompletableFuture().get());
    }
}
