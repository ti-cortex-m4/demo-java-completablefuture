package demo._part13_completable_future.partZ;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class Example1 {

    @Test
    public void testThenApply() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(3);

        future.thenApply(x -> x*x)
                .thenAccept(x -> System.out.print(x))
                .thenRun(() -> System.out.println());

        future.join();
    }
}
