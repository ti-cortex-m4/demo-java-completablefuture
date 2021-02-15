package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Example1 extends Demo1 {

    @Test
    public void testSynchronous() {
        logger.info("this task started");

        int netAmountInUsd = getPriceInEur() * getExchangeRateEurToUsd(); // blocking
        float grossAmountInUsd = netAmountInUsd * (1 + getTax(netAmountInUsd)); // blocking

        logger.info("this task finished: {}", grossAmountInUsd);

        logger.info("another task started");
    }

    @Test
    public void testAsynchronousWithFuture() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        logger.info("this task started");

        Future<Integer> priceInEur = executorService.submit(this::getPriceInEur);
        Future<Integer> exchangeRateEurToUsd = executorService.submit(this::getExchangeRateEurToUsd);

        while (!priceInEur.isDone() || !exchangeRateEurToUsd.isDone()) { // non-blocking
            Thread.sleep(100);
            logger.info("another task is running");
        }

        int netAmountInUsd = priceInEur.get() * exchangeRateEurToUsd.get(); // actually non-blocking
        Future<Float> tax = executorService.submit(() -> getTax(netAmountInUsd));

        while (!tax.isDone()) { // non-blocking
            Thread.sleep(100);
            logger.info("another task is running");
        }

        float grossAmountInUsd = netAmountInUsd * (1 + tax.get()); // actually non-blocking

        logger.info("this task finished: {}", grossAmountInUsd);

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        logger.info("another task is running");
    }

    @Test
    public void testAsynchronousWithCompletableFuture() throws InterruptedException {
        CompletableFuture<Integer> priceInEur = supplyAsync(this::getPriceInEur);
        CompletableFuture<Integer> exchangeRateEurToUsd = supplyAsync(this::getExchangeRateEurToUsd);

        CompletableFuture<Integer> netAmountInUsd = priceInEur
                .thenCombine(exchangeRateEurToUsd, (price, exchangeRate) -> price * exchangeRate);

        logger.info("this task started");

        netAmountInUsd
                .thenCompose(amount -> supplyAsync(() -> amount * (1 + getTax(amount))))
                .whenComplete((grossAmountInUsd, throwable) -> {
                    if (throwable == null) {
                        logger.info("this task finished: {}", grossAmountInUsd);
                    } else {
                        logger.warn("this task failed: {}", throwable.getMessage());
                    }
                }); // non-blocking

        logger.info("another task started");
        Thread.sleep(10000);
    }

    private int getPriceInGbp() {
        return sleepAndGet(1);
    }

    private int getPriceInEur() {
        return sleepAndGet(2);
    }

    private int getExchangeRateGbpToUsd() {
        return sleepAndGet(3);
    }

    private int getExchangeRateEurToUsd() {
        return sleepAndGet(4);
    }

    private float getTax(int amount) {
        return sleepAndGet(50) / 100f;
    }
}
