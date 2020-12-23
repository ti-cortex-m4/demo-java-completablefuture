package demo._part13_completable_future.part1;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class CompleteAsync extends Demo1 {

    @Test
    public void testCompleteAsync() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future1 = new CompletableFuture<>();
        assertFalse(future1.isDone());
        CompletableFuture<String> future2 = future1.completeAsync(() ->"value");
        assertTrue(future2.isDone());
        assertEquals("value", future2.get());
    }
}