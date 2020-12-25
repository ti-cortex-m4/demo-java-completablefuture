package demo._part13_completable_future.part7;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

// wait for all of futures to finish
public class AllOf extends Demo1 {

    @Test
    public void testAllOf() throws InterruptedException, ExecutionException {
        CompletableFuture<?>[] futures = new CompletableFuture<?>[] {
                CompletableFuture.supplyAsync(() -> sleepAndGet(1, "parallel1")),
                CompletableFuture.supplyAsync(() -> sleepAndGet(2, "parallel2")),
                CompletableFuture.supplyAsync(() -> sleepAndGet(3, "parallel3"))
        };

        CompletableFuture<Void> future = CompletableFuture.allOf(futures);
        future.get();

        String result = Stream.of(futures)
                .map(CompletableFuture::join)
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        assertEquals("parallel1, parallel2, parallel3", result);
    }
}
