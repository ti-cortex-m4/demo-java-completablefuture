package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Example1 extends Demo1 {

    @Test
    public void test1() {
        LocalDateTime start = LocalDateTime.now();
        logger.info("started");

        int y = (get(1) * get(2)) + (get(3) * get(4));

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: y={} after {} ms", y, Duration.between(start, finish).toMillis());
    }

    @Test
    public void test2() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        LocalDateTime start = LocalDateTime.now();
        logger.info("started");

        Future<Integer> f1 = executorService.submit(() -> get(1));
        Future<Integer> f2 = executorService.submit(() -> get(2));
        Future<Integer> f3 = executorService.submit(() -> get(3));
        Future<Integer> f4 = executorService.submit(() -> get(4));

        while (!f1.isDone() || !f2.isDone() || !f3.isDone() || !f4.isDone()) {
            Thread.sleep(100);
        }

        int y = (f1.get() * f2.get()) + (f3.get() * f4.get()) ;

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: y={} after {} ms", y, Duration.between(start, finish).toMillis());

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test
    public void test3() throws InterruptedException, ExecutionException {
        LocalDateTime start = LocalDateTime.now();
        logger.info("started");

        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(() -> get(1));
        CompletableFuture<Integer> cf2 = CompletableFuture.supplyAsync(() -> get(2));
        CompletableFuture<Integer> cf3 = CompletableFuture.supplyAsync(() -> get(3));
        CompletableFuture<Integer> cf4 = CompletableFuture.supplyAsync(() -> get(4));

        CompletableFuture<Integer> x1x2 = cf1
                .thenCombine(cf2, (x1, x2) -> x1 * x2);
        CompletableFuture<Integer> x3x4 = cf3
                .thenCombine(cf4, (x1, x2) -> x1 * x2);

        int y = x1x2
                .thenCombine(x3x4, (a, b) -> a + b)
                .get();

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: y={} after {} ms", y, Duration.between(start, finish).toMillis());
    }

    private int get(int i) {
        return sleepAndGet(i);
    }
}
