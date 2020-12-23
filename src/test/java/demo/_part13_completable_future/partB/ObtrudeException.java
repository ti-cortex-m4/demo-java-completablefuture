package demo._part13_completable_future.partB;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class ObtrudeException extends Demo1 {

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
    public void testObtrudeException2() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("error1"));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        future.obtrudeException(new RuntimeException("error2"));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
    }
}
