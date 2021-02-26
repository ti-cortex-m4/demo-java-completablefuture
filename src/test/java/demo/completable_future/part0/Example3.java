package demo.completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class Example3 extends Demo1 {

    @Test
    public void test() {
        CompletableFuture.supplyAsync(() -> 0)
                .thenApply(i -> { logger.info("step 1: {}", i); return 1 / i; }) // step 1: 0
                .thenApply(i -> { logger.info("step 2: {}", i); return 1 / i; }) // skipped
                .whenComplete((value, t) -> {
                    if (t == null) {
                        logger.info("success: {}", value);
                    } else {
                        logger.warn("failure: {}", t.getMessage()); // java.lang.ArithmeticException: / by zero
                    }
                })
                .thenApply(i -> { logger.info("step 3: {}", i); return 1 / i; }) // skipped
                .handle((value, t) -> {
                    if (t == null) {
                        return value + 1;
                    } else {
                        return -1; // executed
                    }
                })
                .thenApply(i -> { logger.info("step 4: {}", i); return 1 / i; }) // step 4: -1
                .join();
    }
}
