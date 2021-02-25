package demo.completable_future.part5;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Complete extends Demo1 {

    @Test
    public void testComplete() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = new CompletableFuture<>();
        assertFalse(future.isDone());
        boolean hasCompleted = future.complete("value");
        assertTrue(hasCompleted);
        assertTrue(future.isDone());
        assertEquals("value", future.get());
    }
}