package demo.part13_completable_future.partZ;

import demo.common.Demo1;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Example2 extends Demo1 {

    private final Map<Integer, String> cache = new ConcurrentHashMap<>();

    private String getLocal(int id) {
        return cache.get(id);
    }

    private String getRemote(int id) {
        try {
            Thread.sleep(1000);
            if (id == 0) {
                throw new RuntimeException("Bad request");
            }
        } catch (InterruptedException ignored) {
        }
        return new String("" + id);
    }

    public CompletableFuture<String> getString(int id) {
        try {
            String String = getLocal(id);
            if (String != null) {
                logger.info("getLocal with id=" + id);
                return CompletableFuture.completedFuture(String);
            } else {
                // Synchronous (simulating legacy system)
                logger.info("getRemote with id=" + id);
                CompletableFuture<String> future = new CompletableFuture<>();
                String p = getRemote(id);
                cache.put(id, p);
                future.complete(p);
                return future;
            }
        } catch (Exception e) {
            logger.info("exception thrown");
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    public CompletableFuture<String> getStringAsync(int id) {
        try {
            String String = getLocal(id);
            if (String != null) {
                logger.info("getLocal with id=" + id);
                return CompletableFuture.completedFuture(String);
            } else {
                logger.info("getRemote with id=" + id);
                // Asynchronous
                return CompletableFuture.supplyAsync(() -> {
                    String p = getRemote(id);
                    cache.put(id, p);
                    return p;
                });
            }
        } catch (Exception e) {
            logger.info("exception thrown");
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Test
    public void test1() {
        getString(0);
        getString(1);
        getString(2);
        getString(3);
    }

    @Test
    public void test2() {
        getStringAsync(0);
        getStringAsync(1);
        getStringAsync(2);
        getStringAsync(3);
    }
}
