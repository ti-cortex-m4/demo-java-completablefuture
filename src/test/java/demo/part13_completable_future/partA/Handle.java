package demo.part13_completable_future.partA;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class Handle extends Demo1 {

    @Test
    public void testWhenCompleteSuccess() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value")
                .handle((value, t) -> {
                    if (t == null) {
                        return value.toUpperCase();
                    } else {
                        return "error";
                    }
                });
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("VALUE", future.get());
    }

    @Test
    public void testWhenCompleteError() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.<String>failedFuture(new RuntimeException())
                .handle((value, t) -> {
                    if (t == null) {
                        return value.toUpperCase();
                    } else {
                        return "error";
                    }
                });
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("error", future.get());
    }
}
