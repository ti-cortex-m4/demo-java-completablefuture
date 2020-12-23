package demo._part13_completable_future.part6;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

// apply result in Function after finishing any future
public class ApplyToEither extends Demo1 {

    @Test
    public void testApplyToEither() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1"))
                .applyToEither(CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                        String::toUpperCase);
        assertEquals("PARALLEL1", future.get());
    }
}