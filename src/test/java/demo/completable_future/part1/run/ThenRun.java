package demo.completable_future.part1.run;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.junit.Assert.assertNull;

public class ThenRun extends Demo1 {

    @Test
    public void testThenRun() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("single"));

        CompletionStage<Void> stage = stage1.thenRun(
                () -> logger.info("runs after the single"));

        assertNull(stage.toCompletableFuture().get());
    }
}
