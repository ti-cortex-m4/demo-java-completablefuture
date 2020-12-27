import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class Demo {

    public static void main(String... args) {
        long count = Arrays.stream(CompletableFuture.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .count();
        System.out.println(count);
    }
}