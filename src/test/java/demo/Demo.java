package demo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class Demo {

    public static void main(String... args) {
        List<String> list = Arrays.stream(CompletionStage.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        list.forEach(System.out::println);
        System.out.println();

        Arrays.stream(CompletableFuture.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .distinct()
                .sorted()
                .filter(name -> !list.contains(name))
                .forEach(System.out::println);
    }
}
