package demo._part13_completable_future.part61;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

// run two futures parallelly and then combine them
public class ThenCombine extends Demo1 {

    @Test
    public void testThenCombine() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("parallel1"))
                .thenCombine(CompletableFuture.supplyAsync(() -> sleepAndGet("parallel2")),
                        (s1, s2) -> s1 + " " + s2);
        assertEquals("parallel1 parallel2", future.get());
    }
}