package demo.completable_future.part8;

import demo.common.Demo;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObtrudeException extends Demo {

    @Test
    public void testObtrudeException1() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("value", future.get());

        future.obtrudeException(new RuntimeException("error"));

        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testObtrudeException2() {
        CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("error1"));

        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());

        future.obtrudeException(new RuntimeException("error2"));

        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
    }
}
