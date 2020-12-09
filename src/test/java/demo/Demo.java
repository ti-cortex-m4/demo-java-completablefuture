package demo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.CompletableFuture;

public class Demo {

    public static void main(String... args) {
        Class<?> clazz = CompletableFuture.class;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                System.out.println(method);
            }
        }
    }
}
