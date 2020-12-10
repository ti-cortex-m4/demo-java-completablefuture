package demo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Demo {

    public static void main(String... args) {
        Arrays.stream(CompletionStage.class.getDeclaredMethods())
                .filter(method ->Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .distinct()
                .sorted()
                .forEach(System.out::println);
    }
}
