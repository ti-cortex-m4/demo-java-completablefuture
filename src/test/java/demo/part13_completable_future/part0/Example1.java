package demo.part13_completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Example1 extends Demo1 {

    @Test
    public void test1() {
        LocalDateTime start = LocalDateTime.now();
        logger.info("started");

        int amountInUsd1 = getPriceInGbp() * getExchangeRateGbpToUsd(); //blocking
        int amountInUsd2 = getPriceInEur() * getExchangeRateEurToUsd(); //blocking
        int amountInUsd = amountInUsd1 + amountInUsd2;
        float amountInUsdAfterTax = amountInUsd * (1 + getTax(amountInUsd)); //blocking

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: result1={} after {} ms", amountInUsdAfterTax, Duration.between(start, finish).toMillis());
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

        while (!priceInGbp.isDone() || !exchangeRateGbpToUsd.isDone()
                || !priceInEur.isDone() || !exchangeRateEurToUsd.isDone()) {
            Thread.sleep(100); // busy-waiting
        }

        int amountInUsd1 = priceInGbp.get() * exchangeRateGbpToUsd.get();
        int amountInUsd2 = priceInEur.get() * exchangeRateEurToUsd.get();
        int amountInUsd = amountInUsd1 + amountInUsd2;

        Future<Float> tax = executorService.submit(() -> getTax(amountInUsd));

        while (!tax.isDone()) {
            Thread.sleep(100); // busy-waiting
        }

        float amountInUsdAfterTax = amountInUsd * (1 + tax.get());

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: result2={} after {} ms", amountInUsdAfterTax, Duration.between(start, finish).toMillis());

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
    }

    @Test
    public void test3() throws InterruptedException, ExecutionException {
        LocalDateTime start = LocalDateTime.now();
        logger.info("started");

        CompletableFuture<Integer> priceInGbp = supplyAsync(this::getPriceInGbp);
        CompletableFuture<Integer> exchangeRateGbpToUsd = supplyAsync(this::getExchangeRateGbpToUsd);
        CompletableFuture<Integer> priceInEur = supplyAsync(this::getPriceInEur);
        CompletableFuture<Integer> exchangeRateEurToUsd = supplyAsync(this::getExchangeRateEurToUsd);

        CompletableFuture<Integer> amountInUsd1 = priceInGbp
                .thenCombine(exchangeRateGbpToUsd, (price, exchangeRate) -> price * exchangeRate);
        CompletableFuture<Integer> amountInUsd2 = priceInEur
                .thenCombine(exchangeRateEurToUsd, (price, exchangeRate) -> price * exchangeRate);

        float amountInUsdAfterTax = amountInUsd1
                .thenCombine(amountInUsd2, (amount1, amount2) -> amount1 + amount2)
                .thenCompose(amountInUsd -> supplyAsync(() -> amountInUsd * (1 + getTax(amountInUsd))))
                .get(); // blocking

        LocalDateTime finish = LocalDateTime.now();
        logger.info("finished: result3={} after {} ms", amountInUsdAfterTax, Duration.between(start, finish).toMillis());
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
