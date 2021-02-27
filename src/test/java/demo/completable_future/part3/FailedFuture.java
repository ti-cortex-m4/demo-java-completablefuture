package demo.completable_future.part3;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;

public class FailedFuture extends Demo1 {

    @Test
    public void testCompletedFuture() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("exception"));

        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testCompletedStage() throws InterruptedException, ExecutionException {
        CompletionStage<String> future = CompletableFuture.failedStage(new RuntimeException("exception"));

        assertTrue(future.toCompletableFuture().isDone());
        assertTrue(future.toCompletableFuture().isCompletedExceptionally());
    }
}

