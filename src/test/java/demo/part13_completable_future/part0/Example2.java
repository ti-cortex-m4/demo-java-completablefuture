package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Example2 extends Demo1 {

    @Test
    public void testThenApply() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage = supplyAsync(() -> sleepAndGet("single"))
                .thenApply(s -> s.toUpperCase());

        assertEquals("SINGLE", stage.toCompletableFuture().get());
    }

    @Test
    public void testThenCompose() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("sequential1"));

        CompletionStage<String> stage = stage1
                .thenCompose(s -> {
                    CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(s + " " + "sequential2"));
                    return stage2;
                });

        assertEquals("sequential1 sequential2", stage.toCompletableFuture().get());
    }

    @Test
    public void testApplyToEither() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
        CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

        CompletionStage<String> stage = stage1.applyToEither(stage2,
                s -> s.toUpperCase());

        assertEquals("PARALLEL1", stage.toCompletableFuture().get());
    }

    @Test
    public void testThenCombine() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("parallel1"));
        CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet("parallel2"));

        CompletionStage<String> stage = stage1.thenCombine(stage2,
                (s1, s2) -> (s1 + " " + s2).toUpperCase());

        assertEquals("PARALLEL1 PARALLEL2", stage.toCompletableFuture().get());
    }


    @Test
    public void testThenAccept() throws InterruptedException, ExecutionException {
        CompletionStage<Void> stage = supplyAsync(() -> sleepAndGet("single"))
                .thenAccept(s -> logger.info("consumed single: {}", s));

        assertNull(stage.toCompletableFuture().get());
    }

    @Test
    public void testAcceptEither() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
        CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

        CompletionStage<Void> stage = stage1.acceptEither(stage2,
                s -> logger.info("consumed first: {}", s));

        assertNull(stage.toCompletableFuture().get());
    }

    @Test
    public void testThenAcceptBoth() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
        CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

        CompletionStage<Void> stage = stage1.thenAcceptBoth(stage2,
                (s1, s2) -> logger.info("consumed both: {} {}", s1, s2));

        assertNull(stage.toCompletableFuture().get());
    }


    @Test
    public void testThenRun() throws InterruptedException, ExecutionException {
        CompletionStage<Void> stage = supplyAsync(() -> sleepAndGet("single"))
                .thenRun(() -> logger.info("run after single"));

        assertNull(stage.toCompletableFuture().get());
    }

    @Test
    public void testRunAfterEither() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
        CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

        CompletionStage<Void> stage = stage1.runAfterEither(stage2,
                () -> logger.info("run after first"));

        assertNull(stage.toCompletableFuture().get());
    }

    @Test
    public void testRunAfterBoth() throws InterruptedException, ExecutionException {
        CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
        CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

        CompletionStage<Void> stage = stage1.runAfterBoth(stage2,
                () -> logger.info("run after both"));

        assertNull(stage.toCompletableFuture().get());
    }
}
