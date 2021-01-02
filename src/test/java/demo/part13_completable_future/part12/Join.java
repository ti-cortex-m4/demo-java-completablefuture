package demo.part13_completable_future.part12;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class Join extends Demo1 {

    @Test
    public void testJoin() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"));
        assertEquals("value", future.join()); // throws no checked exceptions
    }
}
