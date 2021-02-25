package demo.completable_future.part1.accept;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNull;

public class AcceptEither extends Demo1 {

    @Test
    public void testAcceptEither() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .acceptEither(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        s -> logger.info("consumed first: " + s));
        assertNull(future.get());
    }
}
