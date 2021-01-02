package demo.part13_completable_future.part13;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class DelayedExecutor extends Demo1 {

    @Test
    public void testDelayedExecutor() {
        Executor executor = CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS);
        System.out.println("begin");
        executor.execute(() -> System.out.println("Runnable"));
        System.out.println("end");

        sleep(2);
    }
}
