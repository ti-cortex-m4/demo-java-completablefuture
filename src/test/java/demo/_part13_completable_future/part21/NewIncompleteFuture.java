package demo._part13_completable_future.part21;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class NewIncompleteFuture extends Demo1 {

    @Test
    public void testComplete() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("value");
        assertTrue(future1.isDone());
        CompletableFuture<String> future2 = future1.newIncompleteFuture();
        assertFalse(future2.isDone());
        future2.complete("value");
        assertTrue(future2.isDone());
        assertEquals("value", future2.get());
    }
}
