package demo.completable_future.part3;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class SupplyAsync extends Demo1 {

    @Test
    public void testSupplyAsync() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
        assertEquals("value", future.get());
    }
}