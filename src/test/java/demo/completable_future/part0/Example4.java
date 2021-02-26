package demo.completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.*;

public class Example4 extends Demo1 {

    @Test
    public void test() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();


        CompletableFuture<String> future = new CompletableFuture<>(); // creating

        executorService.submit(() -> {
            Thread.sleep(500);
            future.complete("value"); // completing
            return null;
        });

        while (!future.isDone()) { // checking
            Thread.sleep(1000);
        }

        String result = future.get(); // reading
        logger.info("result: {}", result);


        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
    }
}
