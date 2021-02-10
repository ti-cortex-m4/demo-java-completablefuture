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

        int amountInUsd1 = getPriceInGbp() * getExchangeRateGbpToUsd(); // blocking
        int amountInUsd2 = getPriceInEur() * getExchangeRateEurToUsd(); // blocking
        int netAmountInUsd = amountInUsd1 + amountInUsd2;
        float grossAmountInUsd = netAmountInUsd * (1 + getTax(netAmountInUsd)); // blocking

        logger.info("this task finished: {}", grossAmountInUsd);

        logger.info("another task started");
    }

    @Test
    public void testAsynchronousWithFuture() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        logger.info("this task started");

        Future<Integer> priceInGbp = executorService.submit(this::getPriceInGbp);
        Future<Integer> exchangeRateGbpToUsd = executorService.submit(this::getExchangeRateGbpToUsd);
        Future<Integer> priceInEur = executorService.submit(this::getPriceInEur);
        Future<Integer> exchangeRateEurToUsd = executorService.submit(this::getExchangeRateEurToUsd);

        while (!priceInGbp.isDone() || !exchangeRateGbpToUsd.isDone()
                || !priceInEur.isDone() || !exchangeRateEurToUsd.isDone()) { // non-blocking
            Thread.sleep(100);
            logger.info("another task is running");
        }

        int amountInUsd1 = priceInGbp.get() * exchangeRateGbpToUsd.get(); // actually non-blocking
        int amountInUsd2 = priceInEur.get() * exchangeRateEurToUsd.get(); // actually non-blocking
        int netAmountInUsd = amountInUsd1 + amountInUsd2;

        Future<Float> tax = executorService.submit(() -> getTax(netAmountInUsd));

        while (!tax.isDone()) { // non-blocking
            Thread.sleep(100);
            logger.info("another task is running");
        }

        float grossAmountInUsd = netAmountInUsd * (1 + tax.get()); // actually non-blocking

        logger.info("finished: {}", grossAmountInUsd);

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        logger.info("another task is running");
    }

    @Test
    public void testAsynchronousWithCompletableFuture() throws InterruptedException {
        CompletableFuture<Integer> priceInGbp = supplyAsync(this::getPriceInGbp);
        CompletableFuture<Integer> exchangeRateGbpToUsd = supplyAsync(this::getExchangeRateGbpToUsd);
        CompletableFuture<Integer> priceInEur = supplyAsync(this::getPriceInEur);
        CompletableFuture<Integer> exchangeRateEurToUsd = supplyAsync(this::getExchangeRateEurToUsd);

        CompletableFuture<Integer> amountInUsd1 = priceInGbp
                .thenCombine(exchangeRateGbpToUsd, (price, exchangeRate) -> price * exchangeRate);
        CompletableFuture<Integer> amountInUsd2 = priceInEur
                .thenCombine(exchangeRateEurToUsd, (price, exchangeRate) -> price * exchangeRate);

        logger.info("this task started");

        amountInUsd1
                .thenCombine(amountInUsd2, (amount1, amount2) -> amount1 + amount2)
                .thenCompose(netAmountInUsd -> supplyAsync(() -> netAmountInUsd * (1 + getTax(netAmountInUsd))))
                .whenComplete((grossAmountInUsd, throwable) -> {
                    if (throwable == null) {
                        logger.info("this task finished: {}", grossAmountInUsd);
                    } else {
                        logger.info("this task failed: {}", throwable.getMessage());
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
