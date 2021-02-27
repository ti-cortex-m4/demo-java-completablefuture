package demo.completable_future.part8;

import demo.common.Demo;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class DelayedExecutor extends Demo {

    @Test
    public void testDelayedExecutor() {
        Executor executor = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS);

        System.out.println("begin");
        executor.execute(() -> System.out.println("Runnable"));
        System.out.println("end");

        sleep(2);
    }
}

/*
CompletableFuture<Object> future = new CompletableFuture<>();
future.completeAsync(() -> input, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
*/