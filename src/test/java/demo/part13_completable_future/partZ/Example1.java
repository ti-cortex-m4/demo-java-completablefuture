package demo.part13_completable_future.partZ;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Example1 {

    @Test
    public void testThenApply() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(3);

        future.thenApply(x -> x * x)
                .thenAccept(x -> System.out.println("x=" + x))
                .thenRun(() -> System.out.println("finish"));

        future.join();
    }
}
