package demo.completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.*;

public class Example4 extends Demo1 {

    @Test
    public void test() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        CompletableFuture<String> future = new CompletableFuture<>(); // creating an incomplete future

        executorService.submit(() -> {
            Thread.sleep(500);
            future.complete("value"); // completing the incomplete future
            return null;
        });

        while (!future.isDone()) { // checking for the future completion
            Thread.sleep(1000);
        }

        String result = future.get(); // reading value of the completed future
        logger.info("result: {}", result);

        executorService.shutdown();
    }
}
