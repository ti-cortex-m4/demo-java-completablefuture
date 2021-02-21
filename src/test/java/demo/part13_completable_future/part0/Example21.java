package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Example21 extends Demo1 {

    @Test
    public void testThenApply() throws InterruptedException, ExecutionException {
        CompletionStage<Integer> stage = supplyAsync(() -> 2)
                .thenApply(i -> i * i);
        assertEquals(4, stage.toCompletableFuture().get().intValue());
    }

    @Test
    public void testThenCompose() throws InterruptedException, ExecutionException {
        CompletionStage<Integer> stage = supplyAsync(() -> 2)
                .thenCompose(i -> supplyAsync(() -> sleepAndGet(i + 3)));
        assertEquals(5, stage.toCompletableFuture().get().intValue());
    }

    @Test
    public void testApplyToEither() throws InterruptedException, ExecutionException {
        CompletionStage<Integer> stage = supplyAsync(() -> sleepAndGet(2))
                .applyToEither(supplyAsync(() -> 3),
                        i -> i * i);
        assertEquals(9, stage.toCompletableFuture().get().intValue());
    }

    @Test
    public void testThenCombine() throws InterruptedException, ExecutionException {
        CompletionStage<Integer> stage = supplyAsync(() -> sleepAndGet(2))
                .thenCombine(supplyAsync(() -> 3),
                        (i, j) -> i + j);
        assertEquals(5, stage.toCompletableFuture().get().intValue());
    }


    @Test
    public void testThenAccept() throws InterruptedException, ExecutionException {
        CompletionStage<Void> stage = supplyAsync(() -> 2)
                .thenAccept(i -> logger.info("consumes: {}", i));
        assertNull(stage.toCompletableFuture().get());
    }

    @Test
    public void testAcceptEither() throws InterruptedException, ExecutionException {
        CompletionStage<Void> stage = (supplyAsync(() -> sleepAndGet(2)))
                .acceptEither(supplyAsync(() -> 3),
                        i -> logger.info("consumes: {}", i));
        assertNull(stage.toCompletableFuture().get());
    }

    @Test
    public void testThenAcceptBoth() throws InterruptedException, ExecutionException {
        CompletionStage<Void> stage = supplyAsync(() -> sleepAndGet(2))
                .thenAcceptBoth(supplyAsync(() -> 3),
                        (i, j) -> logger.info("consumes: {}, {}", i, j));
        assertNull(stage.toCompletableFuture().get());
    }


    @Test
    public void testThenRun() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("single"));

        CompletionStage<Void> stage = stage1.thenRun(
                () -> logger.info("runs after single"));

        assertNull(stage.toCompletableFuture().get());
    }

    @Test
    public void testRunAfterEither() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
        CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

        CompletionStage<Void> stage = stage1.runAfterEither(stage2,
                () -> logger.info("runs after first"));

        assertNull(stage.toCompletableFuture().get());
    }

    @Test
    public void testRunAfterBoth() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
        CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

        CompletionStage<Void> stage = stage1.runAfterBoth(stage2,
                () -> logger.info("runs after both"));

        assertNull(stage.toCompletableFuture().get());
    }
}
