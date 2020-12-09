package demo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public class Demo {

    public static void main(String... args) {
        Arrays.stream(CompletableFuture.class.getDeclaredMethods())
                .filter(method ->Modifier.isPublic(method.getModifiers()))
                .sorted(Comparator.comparing(Method::getName))
                .forEach(method -> System.out.println(method.getName() + " " + Arrays.toString(method.getParameters())));
    }
}
