package demo.completable_future.part8;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObtrudeValue extends Demo1 {

    @Test
    public void testObtrudeValue1() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("value", future.get());
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        future.obtrudeValue("value2");
        assertEquals("value2", future.get());
    }

    @Test
    public void testObtrudeValue2() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("error"));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        future.obtrudeValue("value");
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("value", future.get());
    }
}
