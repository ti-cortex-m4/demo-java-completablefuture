package demo._part13_completable_future.partC;

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
