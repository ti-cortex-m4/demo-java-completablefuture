package demo.completable_future.part8;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;

public class Copy extends Demo1 {

    @Test
    public void testObtrudeException1() {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("value");
        assertTrue(future1.isDone());

        CompletableFuture<String> future2 = future1.copy();
        assertTrue(future2.isDone());
    }
}
