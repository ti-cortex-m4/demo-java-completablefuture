package demo.part13_completable_future.part13;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class GetNumberOfDependents extends Demo1 {

    @Test
    public void testGetNumberOfDependents() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("value");
        assertEquals(1, future.getNumberOfDependents());
    }
}