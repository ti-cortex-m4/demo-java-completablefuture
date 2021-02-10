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
import java.util.function.BiConsumer;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Example1 extends Demo1 {

    @Test
    public void testSynchronous() {
        LocalDateTime start = LocalDateTime.now();
        logger.info("this task started");

        int amountInUsd1 = getPriceInGbp() * getExchangeRateGbpToUsd(); // blocking
        int amountInUsd2 = getPriceInEur() * getExchangeRateEurToUsd(); // blocking
        int netAmountInUsd = amountInUsd1 + amountInUsd2;
        float grossAmountInUsd = netAmountInUsd * (1 + getTax(netAmountInUsd)); // blocking

        LocalDateTime finish = LocalDateTime.now();
        logger.info("this task finished: result={} after {} ms", grossAmountInUsd, Duration.between(start, finish).toMillis());

        logger.info("another task started");
    }

    @Test
    public void testAsynchronousWithFuture() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        LocalDateTime start = LocalDateTime.now();
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

        int amountInUsd1 = priceInGbp.get() * exchangeRateGbpToUsd.get();
        int amountInUsd2 = priceInEur.get() * exchangeRateEurToUsd.get();
        int netAmountInUsd = amountInUsd1 + amountInUsd2;

        Future<Float> tax = executorService.submit(() -> getTax(netAmountInUsd));

        while (!tax.isDone()) { // non-blocking
            Thread.sleep(100);
            logger.info("another task is running");
        }

        float grossAmountInUsd = netAmountInUsd * (1 + tax.get());

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: result={} after {} ms", grossAmountInUsd, Duration.between(start, finish).toMillis());

        logger.info("another task is running");

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test
    public void testAsynchronousWithCompletableFuture() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> priceInGbp = supplyAsync(this::getPriceInGbp);
        CompletableFuture<Integer> exchangeRateGbpToUsd = supplyAsync(this::getExchangeRateGbpToUsd);
        CompletableFuture<Integer> priceInEur = supplyAsync(this::getPriceInEur);
        CompletableFuture<Integer> exchangeRateEurToUsd = supplyAsync(this::getExchangeRateEurToUsd);

        CompletableFuture<Integer> amountInUsd1 = priceInGbp
                .thenCombine(exchangeRateGbpToUsd, (price, exchangeRate) -> price * exchangeRate);
        CompletableFuture<Integer> amountInUsd2 = priceInEur
                .thenCombine(exchangeRateEurToUsd, (price, exchangeRate) -> price * exchangeRate);

        LocalDateTime start = LocalDateTime.now();
        logger.info("this task started");

        amountInUsd1
                .thenCombine(amountInUsd2, (amount1, amount2) -> amount1 + amount2)
                .thenCompose(netAmountInUsd -> supplyAsync(() -> netAmountInUsd * (1 + getTax(netAmountInUsd))))
                .whenComplete(new BiConsumer<Float, Throwable>() {
                    @Override
                    public void accept(Float result, Throwable throwable) {
                        if (throwable == null) {
                            LocalDateTime finish = LocalDateTime.now();
                            logger.info("this task finished: result={} after {} ms", result, Duration.between(start, finish).toMillis());
                        } else {
                            logger.info("this task failed: {}", throwable.getMessage());
                        }
                    }
                }); // non-blocking

        logger.info("another task started");
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
