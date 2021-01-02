package demo.part13_completable_future.part13;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class Copy extends Demo1 {

    @Test
    public void testObtrudeException1() {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("value");
        CompletableFuture<String> future2 = future1.copy();
    }
}
