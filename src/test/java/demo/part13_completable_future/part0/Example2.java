package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Example2 extends Demo1 {

    @Test
    public void testFuture() throws InterruptedException, ExecutionException {

        // thenApply
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> sleepAndGet("value"))
                .thenApply(String::toUpperCase);
        assertEquals("VALUE", future1.get());

        // thenCompose
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> sleepAndGet("sequential1"))
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> sleepAndGet(s + " sequential2")));
        assertEquals("sequential1 sequential2", future2.get());

        // applyToEither
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .applyToEither(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        String::toUpperCase);
        assertEquals("PARALLEL1", future3.get());

        // thenCombine
        CompletableFuture<String> future4 = CompletableFuture.supplyAsync(() -> sleepAndGet("parallel1"))
                .thenCombine(CompletableFuture.supplyAsync(() -> sleepAndGet("parallel2")),
                        (s1, s2) -> s1 + " " + s2);
        assertEquals("parallel1 parallel2", future4.get());


        // thenAccept
        CompletableFuture<Void> future5 = CompletableFuture.supplyAsync(() -> sleepAndGet("value"))
                .thenAccept(s -> logger.info("consumed: " + s));
        assertNull(future5.get());

        // acceptEither
        CompletableFuture<Void> future6 = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .acceptEither(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        s -> logger.info("consumed one: " + s));
        assertNull(future6.get());

        // thenAcceptBoth
        CompletableFuture<Void> future7 = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        (s1, s2) -> logger.info("consumed both: " + s1 + " " + s2));
        assertNull(future7.get());


        // thenRun
        CompletableFuture<Void> future8 = CompletableFuture.completedFuture("value")
                .thenRun(() -> logger.info("run in Runnable"));
        assertNull(future8.get());

        // runAfterEither
        CompletableFuture<Void> future9 = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .runAfterEither(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        () -> logger.info("finished one"));
        assertNull(future9.get());

        // runAfterBoth
        CompletableFuture<Void> future10 = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .runAfterBoth(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        () -> logger.info("finished both"));
        assertNull(future10.get());
    }
}
