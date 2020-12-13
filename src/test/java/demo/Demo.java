package demo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class Demo {

    public static void main(String... args) {
        List<Method> list = Arrays.stream(CompletionStage.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toList());

        list.forEach(System.out::println);
        System.out.println();

        Arrays.stream(CompletableFuture.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .sorted(Comparator.comparing(Method::getName))
                .filter(name -> !list.contains(name))
                .forEach(System.out::println);
    }
}
