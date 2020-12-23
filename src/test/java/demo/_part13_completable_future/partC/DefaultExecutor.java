package demo._part13_completable_future.partC;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class DefaultExecutor extends Demo1 {

    @Test
    public void testDefaultExecutor() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");
        System.out.println(future.defaultExecutor());
    }
}
