package demo.completable_future.part0;

import demo.common.Demo1;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class Example3 extends Demo1 {

    @Test
    public void test() {
        CompletableFuture<Float> i = CompletableFuture.supplyAsync(() -> 0f);

        float area = i
                .thenApply(x -> {logger.info("step 1: {}",x); return 1 /x; })
                .thenApply(x -> {logger.info("step 2: {}",x); return 1 /x; })
                .whenComplete((value, t) -> {
                    if (t == null) {
                        logger.info("success: {}", value);
                    } else {
                        logger.warn("failure: {}", t.getMessage());
                    }
                })
                .thenApply(x -> {logger.info("step 3: {}",x); return 1 /x; })
                .handle((value, t) -> {
                    if (t == null) {
                        return value + 1;
                    } else {
                        return -1f;
                    }
                })
                .thenApply(x -> {logger.info("step 4"); return 1 /x; })
                .join();


    }
}
