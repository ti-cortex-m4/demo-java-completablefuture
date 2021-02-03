package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Example2 extends Demo1 {

    @Test
    public void testThenApply() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = supplyAsync(() -> sleepAndGet("single"))
                .thenApply(s -> "applied: " + s);
        assertEquals("applied: single", future.get());
    }

    @Test
    public void testThenCompose() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = supplyAsync(() -> sleepAndGet("sequential1"))
                .thenCompose(s -> supplyAsync(() -> sleepAndGet("applied: " + s + " sequential2")));
        assertEquals("applied: sequential1 sequential2", future.get());
    }

    @Test
    public void testApplyToEither() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .applyToEither(supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        s -> "applied first: " + s);
        assertEquals("applied first: parallel1", future.get());
    }

    @Test
    public void testThenCombine() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = supplyAsync(() -> sleepAndGet("parallel1"))
                .thenCombine(supplyAsync(() -> sleepAndGet("parallel2")),
                        (s1, s2) -> "applied both: " + s1 + " " + s2);
        assertEquals("applied both: parallel1 parallel2", future.get());
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
