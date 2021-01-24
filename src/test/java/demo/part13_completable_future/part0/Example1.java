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

        int amountInUsd = (getPriceInGbp() * getExchangeRateGbpToUsd()) + (getPriceInEur() * getExchangeRateEurToUsd());
        // TODO add tax

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: y={} after {} ms", amountInUsd, Duration.between(start, finish).toMillis());
    }

    @Test
    public void test2() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();

        LocalDateTime start = LocalDateTime.now();
        logger.info("started");

        Future<Integer> priceInGbp = executorService.submit(this::getPriceInGbp);
        Future<Integer> exchangeRateGbpToUsd = executorService.submit(this::getExchangeRateGbpToUsd);
        Future<Integer> priceInEur = executorService.submit(this::getPriceInEur);
        Future<Integer> exchangeRateEurToUsd = executorService.submit(this::getExchangeRateEurToUsd);

        while (!priceInGbp.isDone() || !exchangeRateGbpToUsd.isDone() || !priceInEur.isDone() || !exchangeRateEurToUsd.isDone()) {
            Thread.sleep(100);
        }

        int amountInUsd = (priceInGbp.get() * exchangeRateGbpToUsd.get()) + (priceInEur.get() * exchangeRateEurToUsd.get()); // sum in X
        // TODO add tax

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: y={} after {} ms", amountInUsd, Duration.between(start, finish).toMillis());

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test
    public void test3() throws InterruptedException, ExecutionException {
        LocalDateTime start = LocalDateTime.now();
        logger.info("started");

        CompletableFuture<Integer> priceInGbp = CompletableFuture.supplyAsync(this::getPriceInGbp);
        CompletableFuture<Integer> exchangeRateGbpToUsd = CompletableFuture.supplyAsync(this::getExchangeRateGbpToUsd);
        CompletableFuture<Integer> priceInEur = CompletableFuture.supplyAsync(this::getPriceInEur);
        CompletableFuture<Integer> exchangeRateEurToUsd = CompletableFuture.supplyAsync(this::getExchangeRateEurToUsd);

        CompletableFuture<Integer> amountInUsd1 = priceInGbp
                .thenCombine(exchangeRateGbpToUsd, (price, exchangeRate) -> price * exchangeRate);
        CompletableFuture<Integer> amountInUsd2 = priceInEur
                .thenCombine(exchangeRateEurToUsd, (price, exchangeRate) -> price * exchangeRate);

        int amountInUsd = amountInUsd1
                .thenCombine(amountInUsd2, (amount1, amount2) -> amount1 + amount2)
                .get();
        // TODO add tax

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: y={} after {} ms", amountInUsd, Duration.between(start, finish).toMillis());
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
}
