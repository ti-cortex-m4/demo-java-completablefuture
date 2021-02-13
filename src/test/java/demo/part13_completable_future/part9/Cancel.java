package demo.part13_completable_future.part9;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// cancel future, cause CancellationException
public class Cancel extends Demo1 {

    @Test
    public void testCancel() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = new CompletableFuture<>();
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
        boolean isCanceled = future.cancel(false);
        assertTrue(isCanceled);
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        try {
            future.get();
            fail();
        } catch (CancellationException e) {
            assertTrue(true);
            //assertEquals(TimeoutException.class, e.getCause().getClass());
        }
    }
}
