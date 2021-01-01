package demo.common;

import java.util.concurrent.TimeUnit;

public class Demo1 extends Demo0 {

    protected static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected static <T> T sleepAndGet(int seconds, T message) {
        logger.info(message + " started");
        sleep(seconds);
        logger.info(message + " finished");
        return message;
    }

    protected static <T> T sleepAndGet(T message) {
        return sleepAndGet(1, message);
    }
}
