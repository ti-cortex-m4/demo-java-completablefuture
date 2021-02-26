package demo.completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.*;

public class Example4 extends Demo1 {

    @Test
    public void test() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();


        CompletableFuture<String> future = new CompletableFuture<>();
        logger.info("future is incomplete");

        executorService.submit(() -> {
            Thread.sleep(500);
            logger.info("future is completing");

            future.complete("Hello");
            return null;
        });

        while (!future.isDone()) {
            Thread.sleep(1000);
            logger.info("waiting");
        }

        logger.info("future is completed");
        String result = future.get();
        logger.info("result: {}", result);


        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
    }
}
