package demo.part13_completable_future.part9;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompleteAsync extends Demo1 {

    @Test
    public void testCompleteAsync() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future1 = new CompletableFuture<>();
        assertFalse(future1.isDone());

        CompletableFuture<String> future2 = future1.completeAsync(() -> "value");
        sleep(1);
        assertTrue(future2.isDone());
        assertEquals("value", future2.get());
    }
}
