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

        int y = (getVariable(1) + getVariable(2)) * getVariable(3);

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: y={} after {} ms", y, Duration.between(start, finish).toMillis());
    }

    @Test
    public void test2() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        LocalDateTime start = LocalDateTime.now();
        logger.info("started");

        Future<Integer> f1 = executorService.submit(() -> getVariable(1));
        Future<Integer> f2 = executorService.submit(() -> getVariable(2));
        Future<Integer> f3 = executorService.submit(() -> getVariable(3));

        while (!f1.isDone() || !f2.isDone() || !f3.isDone()) {
            Thread.sleep(100);
        }

        int y = (f1.get() + f2.get()) * f3.get();

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: y={} after {} ms", y, Duration.between(start, finish).toMillis());

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test
    public void test3() throws InterruptedException, ExecutionException {
        LocalDateTime start = LocalDateTime.now();
        logger.info("started");

        CompletableFuture<Integer> cf1 = CompletableFuture.supplyAsync(() -> getVariable(1));
        CompletableFuture<Integer> cf2 = CompletableFuture.supplyAsync(() -> getVariable(2));
        CompletableFuture<Integer> cf3 = CompletableFuture.supplyAsync(() -> getVariable(3));
        int y = cf1
                .thenCombine(cf2, (x1, x2) -> x1 + x2)
                .thenCombine(cf3, (x1x2, x3) -> x1x2 * x3)
                .get();

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: y={} after {} ms", y, Duration.between(start, finish).toMillis());
    }

    private int getVariable(int i) {
        return sleepAndGet(i);
    }
}
