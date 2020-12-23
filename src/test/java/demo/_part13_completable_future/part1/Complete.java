package demo._part13_completable_future.part1;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

// create future and then complete it
public class Complete extends Demo1 {

    @Test
    public void testComplete() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = new CompletableFuture<>();
        assertFalse(future.isDone());
        future.complete("value");
        assertTrue(future.isDone());
        assertEquals("value", future.get());
    }
}
