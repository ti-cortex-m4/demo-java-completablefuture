package demo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class Demo {

    public static void main(String... args) {
        Arrays.stream(CompletableFuture.class.getDeclaredMethods())
                .filter(method ->Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .distinct()
                .sorted()
                .forEach(System.out::println);
    }
}
