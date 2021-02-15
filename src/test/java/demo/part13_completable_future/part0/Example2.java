package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Example2 extends Demo1 {

    @Test
    public void testThenApply() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = supplyAsync(() -> sleepAndGet("single"))
                .thenApply(value -> value.toUpperCase());

        assertEquals("SINGLE", future.get());
    }

    @Test
    public void testThenCompose() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future1 = supplyAsync(() -> sleepAndGet("sequential1"));

        CompletableFuture<String> future = future1
                .thenCompose(value -> {
                    CompletableFuture<String> future2 = supplyAsync(() -> sleepAndGet(value + " " + "sequential2"));
                    return future2;
                });

        assertEquals("sequential1 sequential2", future.get());
    }

    @Test
    public void testApplyToEither() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
        CompletableFuture<String> future2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

        CompletableFuture<String> future = future1.applyToEither(future2,
                value -> value.toUpperCase());

        assertEquals("PARALLEL1", future.get());
    }

    @Test
    public void testThenCombine() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future1 = supplyAsync(() -> sleepAndGet("parallel1"));
        CompletableFuture<String> future2 = supplyAsync(() -> sleepAndGet("parallel2"));

        CompletableFuture<String> future = future1.thenCombine(future2,
                (value1, value2) -> (value1 + " " + value2).toUpperCase());

        assertEquals("PARALLEL1 PARALLEL2", future.get());
    }


    @Test
    public void testThenAccept() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = supplyAsync(() -> sleepAndGet("single"))
                .thenAccept(s -> logger.info("consumed: " + s));
        assertNull(future.get());
    }

    @Test
    public void testAcceptEither() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .acceptEither(supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        s -> logger.info("consumed first: " + s));
        assertNull(future.get());
    }

    @Test
    public void testThenAcceptBoth() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .thenAcceptBoth(supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        (s1, s2) -> logger.info("consumed both: " + s1 + " " + s2));
        assertNull(future.get());
    }


    @Test
    public void testThenRun() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = supplyAsync(() -> sleepAndGet("single"))
                .thenRun(() -> logger.info("run"));
        assertNull(future.get());
    }

    @Test
    public void testRunAfterEither() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .runAfterEither(supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        () -> logger.info("run after first"));
        assertNull(future.get());
    }

    @Test
    public void testRunAfterBoth() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .runAfterBoth(supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        () -> logger.info("run after both"));
        assertNull(future.get());
    }
}
