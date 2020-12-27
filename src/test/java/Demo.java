import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class Demo {

    public static void main(String... args) {
        long count = Arrays.stream(CompletionStage.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .count();
        System.out.println(count);
    }
}