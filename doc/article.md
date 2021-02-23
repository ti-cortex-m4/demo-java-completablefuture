# Asynchronous programming in Java with CompletableFuture


## Introduction

The CompletableFuture API is a high-level API for asynchronous programming in Java. This API supports _pipelining_ (also known as _chaining_ or _combining_) of multiple asynchronous computations into a single result without the mess of nested callbacks (“callback hell“). This API also is an implementation of the _future/promise_ concurrency constructs in Java.

Since Java 5 there is a much simpler API for asynchronous programming: the `Future` interface and its base implementation, the `FutureTask` class. The `Future` interface represents the result of asynchronous computation and has only a few methods:



*   to check if a task is completed or canceled
*   to cancel a task
*   to wait for a task to complete (if necessary) and then to get its result

However, the `Future` interface has significant limitations in building non-trivial asynchronous computations:



*   it is impossible to register a callback for a future competition
*   it is impossible to pipeline futures in a non-blocking manner
*   it is impossible to manually complete a future

To overcome these limitations, Java 8 added (and Java 9 and Java 12 updated) the `CompletionStage` interface and its base implementation, the `CompletableFuture` class. These classes allow building efficient and fluent multi-stage asynchronous computations, where stages can be forked, chained, and joined.

However, the CompletableFuture API is not simple. The `CompletionStage` interface has 43 public methods. The `CompletableFuture` class implements 5 methods from the `Future` interface, 43 methods from the `CompletionStage` interface, and has 30 of its public methods.


## Futures and promises

Future/promise is high-level concurrency constructs that decouples a value (a future) from how it is computed (a promise). That allows writing more fluent concurrent programs that transfer objects between threads without using any explicit synchronization mechanisms for _shared mutable objects_ like mutexes. The futures/promises construct works very well when multiple threads work on different tasks and the results need to be combined by the main thread.

Implementations of future/promise exist in many programming languages:



*   JavaScript: `Promise`
*   Java: `java.util.concurrent.Future`, `java.util.concurrent.CompletableFuture`
*   Scala: `scala.concurrent.Future`
*   C#: `Task`, `TaskCompletionSource`

Concepts of futures and promises are often used interchangeably. In reality, they are separate objects that encapsulate the two different sets of functionality.

A future is a read-only object to encapsulate a value that may not be available yet but will be provided at some point. The future is used by a consumer to retrieve the result which was computed. A promise is a writable, single-assignment object to guarantee that some task will compute some result and make it available in the future. The promise is used by a producer to store the success value or exception in the corresponding future.

The following workflow example can help you to understand the idea of future/promise. A consumer sends a task to a producer to execute it asynchronously. The producer creates a promise that starts the given task. From the promise, the producer extracts the future and sends it to the consumer. The consumer receives the future that is not completed and waits for its completion.

The consumer can call a blocking getter of the future to wait for the data to be available. If the future has already been completed, the call to the getter will return the result immediately. Otherwise, the call to the getter will wait until the future is finished. (Also, the consumer can use a non-blocking validation method to verify whether the future has already been completed or not).

Once the task has finished, the producer sets the value of the promise and the future becomes available. But when the task fails, the future will contain an exception instead of a success value. So when the consumer calls the getter method, the exception in the future will be thrown.

![future and promise workflow](/images/future_and_promise_workflow.png)

One of the important features of future/promise implementations can be the ability to chain computations together. The idea is that when one future/promise is finished another future/promise is created that takes the result of the previous one. This means that the consumer is not blocked by calling the getter on a future and once the future is completed, the result of the previous computation is automatically passed to the next computation in the chain. Compared to callbacks, this allows writing more fluent asynchronous code that supports the composition of nested success and failure handlers without ”callback hell”.

In Java, the `Future` interface represents a future: it has the `isDone` method to check if the task is completed and the `get` method to wait for the task to complete and get its result. The `CompletableFuture` class represents a promise: it has the `complete` and `completeExceptionally` methods to set the result of the task with a successful value or with an exception, respectively. However, the `CompletableFuture` class also implements the `Future` interface allowing it to be used as a future as well.

![Java futures class diagram](/images/Java_futures_class_diagram.png)


## CompletableFuture in practice

The following code example can help you to understand the use of the `CompletableFuture` class as a future/promise implementation in Java.

Let’s implement the following simplified multi-stage workflow. There are two long-running methods that return a product price in the EUR and the EUR/USD exchange rate. The net product price is calculated from the results of these methods. There is a third long-running method that takes the net product price and returns the tax amount. The gross value is calculated from the net product price and the tax amount.

To implement this workflow, it is necessary to execute the following tasks:



1. to get the product price in the EUR (a slow task)
2. to get the EUR/USD exchange rate (a slow task)
3. to calculate the net product price in the USD (a fast task, depends on tasks 1, 2)
4. to get the amount of the tax (a slow task, depends on tasks 3)
5. to calculate the gross product price in the USD (a fast task, depends on tasks 3, 4)

It is important that not all tasks are the same. Some of them are fast (they should be executed synchronously), and some of them are slow (they should be executed asynchronously). Some of them are independent (they can be executed in parallel), and some of them depend on the results of previous tasks (they have to be executed sequentially).

The mentioned workflow is implemented below in three programming styles: synchronous, asynchronous based on `Future`, and asynchronous based on `CompletableFuture`.

>In _synchronous_ programming, the main thread starts an axillary task and blocks until this task is finished. When the axillary task is completed, the main thread continues the main task.

>In _asynchronous_ programming, the main thread starts an axillary task in a worker thread and continues its task. When the worker thread completes the auxiliary task, it notifies the main thread (for example) with a callback call.

1) The advantage of the synchronous implementation is the simplest and most reliable code. The disadvantage of this implementation is the longest execution time (because all tasks run sequentially).


```
logger.info("this task started");

int netAmountInUsd = getPriceInEur() * getExchangeRateEurToUsd(); // blocking
float tax = getTax(netAmountInUsd); // blocking
float grossAmountInUsd = netAmountInUsd * (1 + tax);

logger.info("this task finished: {}", grossAmountInUsd);
logger.info("another task started");
```


2) The advantage of the asynchronous implementation based on `Future` is shorter execution time (because some tasks run in parallel). The disadvantage of this implementation is the most complicated code (because the `Future` interface lacks methods for tasks pipelining).


```
logger.info("this task started");

Future<Integer> priceInEur = executorService.submit(this::getPriceInEur);
Future<Integer> exchangeRateEurToUsd = executorService.submit(this::getExchangeRateEurToUsd);

while (!priceInEur.isDone() || !exchangeRateEurToUsd.isDone()) { // non-blocking
   Thread.sleep(100);
   logger.info("another task is running");
}

int netAmountInUsd = priceInEur.get() * exchangeRateEurToUsd.get(); // actually non-blocking
Future<Float> tax = executorService.submit(() -> getTax(netAmountInUsd));

while (!tax.isDone()) { // non-blocking
   Thread.sleep(100);
   logger.info("another task is running");
}

float grossAmountInUsd = netAmountInUsd * (1 + tax.get()); // actually non-blocking

logger.info("this task finished: {}", grossAmountInUsd);
logger.info("another task is running");
```


3) The advantage of the asynchronous implementation based on `CompletableFuture` is shorter execution time (because some tasks run in parallel) and more fluent code. The disadvantage of this implementation is that the more advanced CompletableFuture API is at the same time more difficult to learn.


```
CompletableFuture<Integer> priceInEur = supplyAsync(this::getPriceInEur);
CompletableFuture<Integer> exchangeRateEurToUsd = supplyAsync(this::getExchangeRateEurToUsd);

CompletableFuture<Integer> netAmountInUsd = priceInEur
       .thenCombine(exchangeRateEurToUsd, (price, exchangeRate) -> price * exchangeRate);

logger.info("this task started");

netAmountInUsd
       .thenCompose(amount -> supplyAsync(() -> amount * (1 + getTax(amount))))
       .whenComplete((grossAmountInUsd, throwable) -> {
           if (throwable == null) {
               logger.info("this task finished: {}", grossAmountInUsd);
           } else {
               logger.warn("this task failed: {}", throwable.getMessage());
           }
       }); // non-blocking

logger.info("another task started");
```


![CompletableFuture API example](/images/CompletableFuture_API_example.png)


## The [CompletionStage](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/util/concurrent/CompletionStage.html) interface

The `CompletionStage` interface represents a stage in a multi-stage (possibly asynchronous) computation where stages can be _forked, chained, and joined_.

This interface specifies _the pipelining_ of the future/promise implementation in the CompletableFuture API:



1. Each stage performs a computation. A stage can or can not require arguments. A stage can either compute a value (returns a single result) or performs an action (returns no result).
2. Stages can be chained in a pipeline. A stage can be started by finishing a single previous stage (or two previous stages) in the pipeline. A stage finishes when its computation is completed. Finishing a stage can start a single next stage in the pipeline.
3. A stage can be executed synchronously or asynchronously. The appropriate execution type should be selected depending on the nature of the computation.

The methods of the `CompletionStage` interface can be divided into two groups according to their purpose:



*   methods to pipeline computations
*   methods to handle exceptions

![methods of the CompletionStage interface](/images/methods_of_the_CompletionStage_interface.png)

The `CompletionStage` interface contains only methods for stages pipelining. But this interface does not contain methods for other parts of stages workflow: creating, checking, completing, reading. This functionality is delegated to the `CompletableFuture` class - the main implementation of the `CompletionStage` interface.


### Methods to pipeline computations

The `CompletionStage` interface has 43 public methods, most of which follow three naming patterns.

The first naming pattern explains _how the new stage is started_:



*   if a method name has fragment `then`, then the new stage is started after completion of a single previous stage
*   if a method name has fragment `either`, then the new stage is started after completion of the first of two previous stages
*   if a method name has fragment `both`, then the new stage is started after completion of both of two previous stages

The second naming pattern explains _what computations perform the new stage_:



*   if a method name has fragment `apply`, then the new stage transforms an argument by the given `Function`
*   if a method name has fragment `accept`, then the new stage consumes an argument by the given `Consumer`
*   if a method name has fragment `run`, then the new stage runs an action by the given `Runnable`

>If the new stage depends on both of the two previous stages, it uses `BiFunction` instead of `Function` and `BiConsumer` instead of `Consumer`.

Summary of methods to pipeline computations:


<table>
  <tr>
   <td>
   </td>
   <td><code>Function</code>
<p>
(takes an argument and returns a result)
   </td>
   <td><code>Consumer</code>
<p>
(takes an argument and returns no result)
   </td>
   <td><code>Runnable</code>
<p>
(takes no argument and returns no result)
   </td>
  </tr>
  <tr>
   <td><em>then</em>
   </td>
   <td><code>thenApply</code>, <code>thenCompose</code>
   </td>
   <td><code>thenAccept</code>
   </td>
   <td><code>thenRun</code>
   </td>
  </tr>
  <tr>
   <td><em>either</em>
   </td>
   <td><code>applyToEither</code>
   </td>
   <td><code>acceptEither</code>
   </td>
   <td><code>runAfterEither</code>
   </td>
  </tr>
  <tr>
   <td>
   </td>
   <td>
   </td>
   <td>
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>
   </td>
   <td><code>BiFunction</code>
<p>
(takes two arguments and returns a result)
   </td>
   <td><code>BiConsumer</code>
<p>
(takes two arguments and returns no result)
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td><em>both</em>
   </td>
   <td><code>thenCombine</code>
   </td>
   <td><code>thenAcceptBoth</code>
   </td>
   <td><code>runAfterBoth</code>
   </td>
  </tr>
</table>


>If a method accepts a functional interface that does not return a result (`Consumer`, `BiConsumer`, `Runnable`) it can be used to perform a computation with _side-effects_ and to signal that the computation has completed either with a result or with an exception.

The third naming pattern explains _what thread executes the new stage_:



*   if a method has fragment `something(...)`, then the new stage is executed by _the default facility_ (that can be synchronous or asynchronous)
*   if a method has fragment `somethingAsync(...)`, then the new stage is executed by _the default asynchronous facility_
*   if a method has fragment `somethingAsync(..., Executor)`, then the new stage is executed by the given `Executor`

Note that _the default facility_ and _the default asynchronous facility_ are specified by the `CompletionStage` implementations, not by this interface. Looking ahead, the `CompletableFuture` class uses threads that are completing the stage as _the default facility_ and the thread pool returned by the `ForkJoinPool.commonPool()` method as _the default asynchronous facility_.

>Note that the thread pool returned by the `ForkJoinPool.commonPool()` method is shared across a JVM by all `CompletableFuture` objects and all Parallel Streams.


#### Code examples

The `thenApply` method creates a new stage, that upon completion transforms the result of the single previous stage by the given `Function`.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("single"));
CompletionStage<String> stage = stage1.thenApply(
       s -> s.toUpperCase());
assertEquals("SINGLE", stage.toCompletableFuture().get());
```


The `thenCompose` method creates a new stage, that upon completion also transforms the result of the single previous stage by the given `Function`. This method is similar to the `thenApply` method described above. The difference is that the result of the `Function` is a subclass of `CompletionStage`, which is useful when a transformation is a long operation that is reasonable to execute ins a separate stage (possible asynchronously).


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("sequential1"));
CompletionStage<String> stage = stage1.thenCompose(
       s -> supplyAsync(() -> sleepAndGet((s + " " + "sequential2").toUpperCase())));
assertEquals("SEQUENTIAL1 SEQUENTIAL2", stage.toCompletableFuture().get());
```


>You should use the `thenApply` method if you want to transform a `CompletionStage` with a _fast_ function.

>You should use the `thenCompose` method if you want to transform a `CompletionStage` with a _slow_ function.

The `applyToEither` method creates a new stage, that upon completion transforms the first result of the previous two stages by the given `Function`.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));
CompletionStage<String> stage = stage1.applyToEither(stage2,
       s -> s.toUpperCase());
assertEquals("PARALLEL1", stage.toCompletableFuture().get());
```


The `thenCombine` method creates a new stage, that upon completion transforms the two results of the previous two stages by the given `BiFunction`.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet("parallel2"));
CompletionStage<String> stage = stage1.thenCombine(stage2,
       (s1, s2) -> (s1 + " " + s2).toUpperCase());
assertEquals("PARALLEL1 PARALLEL2", stage.toCompletableFuture().get());
```


>You should use the `thenCompose` method if you want to transform two `CompletionStage`s _sequentially_.

>You should use the `thenCombine` method if you want to transform two `CompletionStage`s _in parallel_.

The `thenAccept` method creates a new stage, that upon completion consumes the single previous stage by the given `Consumer`.


```
CompletableFuture<String> stage1 = supplyAsync(() -> sleepAndGet("single"));
CompletionStage<Void> stage = stage1.thenAccept(
       s -> logger.info("consumes the single: {}", s));
assertNull(stage.toCompletableFuture().get());
```


The `acceptEither` method creates a new stage, that upon completion consumes the first result of the previous two stages by the given `Consumer`.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));
CompletionStage<Void> stage = stage1.acceptEither(stage2,
       s -> logger.info("consumes the first: {}", s));
assertNull(stage.toCompletableFuture().get());
```


The `thenAcceptBoth` method creates a new stage,  that upon completion consumes the two results of the previous two stages by the given `BiConsumer`.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));
CompletionStage<Void> stage = stage1.thenAcceptBoth(stage2,
       (s1, s2) -> logger.info("consumes both: {} {}", s1, s2));
assertNull(stage.toCompletableFuture().get());
```


The `thenRun` method creates a new stage, that upon completion of the single previous stage runs the given `Runnable`.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("single"));
CompletionStage<Void> stage = stage1.thenRun(
       () -> logger.info("runs after the single"));
assertNull(stage.toCompletableFuture().get());
```


The `runAfterEither` method creates a new stage, that upon completion of the first of the previous two stages, runs the given `Runnable`.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));
CompletionStage<Void> stage = stage1.runAfterEither(stage2,
       () -> logger.info("runs after the first"));
assertNull(stage.toCompletableFuture().get());
```


The `runAfterBoth` method creates a new stage, that upon completion of the previous two stages, runs the given `Runnable`.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));
CompletionStage<Void> stage = stage1.runAfterBoth(stage2,
       () -> logger.info("runs after both"));
assertNull(stage.toCompletableFuture().get());
```



### Methods to handle exceptions

Each computation may complete normally or exceptionally. In asynchronous computations, the source of the exception and the recovery method can be in different threads. Therefore, in this situation, it is not possible to use a `try-catch-finally` statement to recover from exceptions. So the `CompletionStage` interface has special specifications to handle exceptions.

Each stage has two types of completion of equal importance: normal completion and exceptional completion. If a stage completes normally, the dependent stages start to execute normally. If a stage completes exceptionally, the dependent stages complete exceptionally, unless there is an exception recovery method in the computation pipeline.

Summary of methods to handle exceptions:


<table>
  <tr>
   <td>When the method is called
   </td>
   <td>What the method returns
   </td>
   <td>Method
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td rowspan="2" >called on success or exception
   </td>
   <td>the same result or exception
   </td>
   <td><code>whenComplete(biConsumer)</code>
   </td>
   <td>returns a new `CompletionStage` that upon <em>norman or exceptional</em> completion consumes the result or exception of this stage and returns <em>the same result or exception without modifying them</em>
   </td>
  </tr>
  <tr>
   <td rowspan="2" >a new result
   </td>
   <td><code>handle(biFunction)</code>
   </td>
   <td>returns a new `CompletionStage` that upon <em>norman or exceptional</em> completion transforms the result or exception of this stage and returns <em>the new result</em>
   </td>
  </tr>
  <tr>
   <td>called on exception
   </td>
   <td><code>exceptionally(function)</code>
   </td>
   <td>returns a new `CompletionStage` that upon <em>exceptional</em> completion transforms the exception of this stage and returns <em>the new result</em>
   </td>
  </tr>
</table>


If you need to perform some action, when the previous stage completes normally or exceptionally, you should use the `whenComplete` method. A `BiConsumer` argument of the `whenComplete` method is called when the previous stage completes normally or exceptionally and allows to read both the result (or `null` if none) and the exception (or `null` if none), but does not allow to modify them.

If you need to recover from an exception (to replace the exception with some fallback value), then you should use the `handle` and `exceptionally` methods. A `BiFunction` argument of the `handle` method is called when the previous stage completes normally or exceptionally. A `Function` argument of the `exceptionally` method is called when the previous stage completes exceptionally. In both cases, an exception is not propagated to the next stage.


#### Code examples

The `whenComplete` method accepts a nullable result and an exception but can not modify the return value, the stage is still completed exceptionally.


```
CompletableFuture<String> future = CompletableFuture.<String>failedFuture(new RuntimeException("exception"))
       .whenComplete((value, t) -> {
           if (t == null) {
               logger.info("success: {}", value);
           } else {
               logger.warn("failure: {}", t.getMessage());
           }
       });
assertTrue(future.isDone());
assertTrue(future.isCompletedExceptionally());
```


The `handle` method transforms a nullable result and an exception and converts the result from completed exceptionally to completed normally.


```
CompletableFuture<String> future = CompletableFuture.<String>failedFuture(new RuntimeException("exception"))
       .handle((value, t) -> {
           if (t == null) {
               return value.toUpperCase();
           } else {
               return "failure: " + t.getMessage();
           }
       });
assertTrue(future.isDone());
assertFalse(future.isCompletedExceptionally());
assertEquals("failure: exception", future.get());
```


The `exceptionally` method transforms an exception and converts the result from completed exceptionally to completed normally.


```
CompletableFuture<String> future = CompletableFuture.<String>failedFuture(new RuntimeException("exception"))
       .exceptionally(t -> "failure: " + t.getMessage());
assertTrue(future.isDone());
assertFalse(future.isCompletedExceptionally());
assertEquals("failure: exception", future.get());
```



## The [CompletableFuture](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/CompletableFuture.html) class

The `CompletableFuture` class represents a stage in a multi-stage (possibly asynchronous) computation where stages can be _created, checked, completed_, and _read_. The `CompletableFuture` class is the main implementation of the `CompletionStage` interface, it also implements the `Future` interface. That means the `CompletableFuture` class can simultaneously represent _a stage_ in a multi-stage computation and _the result_ of such a computation.

This class specifies _the general workflow_ of the future/promise implementation in the CompletableFuture API:



1. A _creating_ thread creates an incomplete future and adds computation handlers to it.
2. A _reading_ thread waits (in a blocking or non-blocking manner) until the future is completed normally or exceptionally.
3. A _completing_ thread completes the future and unblocks the _reading _thread.

The methods of the `CompletionStage` interface can be divided into five groups according to their purpose:



*   methods to create futures
*   methods to check futures
*   methods to complete futures
*   methods to read futures
*   methods for bulk futures operations

![methods of the CompletableFuture class](/images/methods_of_the_CompletableFuture_class.png)


### Methods to create futures

By the most general case, a future is created incomplete in a _creating_ thread, is completed in a _completing_ thread, and is read in the _reading_ thread. However, in some cases, one or many threads may be the same.

For that purpose, the `CompletableFuture` class has methods to create futures in different states:



*   methods to create incomplete futures (when the creating thread, the completing thread, and the reading thread are different)
*   methods to create asynchronously completing futures (when the same thread creates a future and completes it)
*   methods to create already completed futures (when the same thread creates a future, completes it, and reads its result)

Summary of methods to create futures


<table>
  <tr>
   <td>Future status
   </td>
   <td>Method name
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td>incomplete
   </td>
   <td><code>newIncompleteFuture()</code>
   </td>
   <td>returns a new incomplete `CompletableFuture`
   </td>
  </tr>
  <tr>
   <td rowspan="2" >asynchronously completing
   </td>
   <td><code>supplyAsync(supplier)</code>
   </td>
   <td>returns a new `CompletableFuture` that is asynchronously completed after it obtains a value from the given `Supplier`
   </td>
  </tr>
  <tr>
   <td><code>runAsync(runnable)</code>
   </td>
   <td>returns a new `CompletableFuture` of that is asynchronously completed after it runs an action from the given `Runnable`
   </td>
  </tr>
  <tr>
   <td rowspan="2" >completed
   </td>
   <td><code>completedFuture(value)​</code>
   </td>
   <td>returns a new `CompletableFuture` that is already completed with the given value
   </td>
  </tr>
  <tr>
   <td><code>failedFuture(throwable)</code>
   </td>
   <td>returns a new `CompletableFuture` that is already completed exceptionally with the given exception
   </td>
  </tr>
</table>


>The no-arg constructor of `CompletableFuture` also creates an incomplete future.


#### Code examples

The no-arg constructor creates an incomplete future.


```
CompletableFuture<String> future = new CompletableFuture<>();
assertFalse(future.isDone());
```


The `newIncompleteFuture` method creates an incomplete future of the same type as the called `CompletableFuture` object. This method should be overridden if you are implementing a subclass of `CompletableFuture`.


```
CompletableFuture<String> future1 = CompletableFuture.completedFuture("value");
assertTrue(future1.isDone());
CompletableFuture<String> future2 = future1.newIncompleteFuture();
assertFalse(future2.isDone());
```


The `supplyAsync` method creates an incomplete future that is asynchronously completed after it obtains a value from the given `Supplier`.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
assertEquals("value", future.get());
```


The `runAsync` method creates an incomplete future that is asynchronously completed after it runs an action from the given `Runnable`.


```
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> logger.info("action"));
assertNull(future.get());
```


The `completedFuture​` method creates a normally completed future.


```
CompletableFuture<String> future = CompletableFuture.completedFuture("value");
assertTrue(future.isDone());
assertFalse(future.isCompletedExceptionally());
assertEquals("value", future.get());
```


The `failedFuture​` method creates an exceptionally completed future.


```
CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("exception"));
assertTrue(future.isDone());
assertTrue(future.isCompletedExceptionally());
```



### Methods to check futures

To check futures, the `CompletableFuture` class has _non-blocking_ methods to verify if a future is incomplete, completed normally, completed exceptionally, or canceled.

Summary of the methods to check futures


<table>
  <tr>
   <td>Method behavior
   </td>
   <td>Method name
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td rowspan="3" >non-blocking
   </td>
   <td><code>isDone</code>
   </td>
   <td>returns <code>true</code> if the <code>CompletableFuture</code> is completed in any manner: normally, exceptionally, or via cancellation
   </td>
  </tr>
  <tr>
   <td><code>isCompletedExceptionally</code>
   </td>
   <td>returns <code>true</code> if this <code>CompletableFuture</code> is completed exceptionally, including cancellation
   </td>
  </tr>
  <tr>
   <td><code>isCancelled</code>
   </td>
   <td>returns <code>true</code> if this <code>CompletableFuture</code> is canceled <em>before it completed normally</em>
   </td>
  </tr>
</table>


>It is impossible to cancel an already completed future.


#### Code examples

The `isDone` method returns `true` because the future is completed.


```
CompletableFuture<String> future = CompletableFuture.completedFuture("value");
assertTrue(future.isDone());
assertFalse(future.isCompletedExceptionally());
assertFalse(future.isCancelled());
assertEquals("value", future.get());
```


The `isCompletedExceptionally` method returns `true` because the future is completed exceptionally. Note that a future _completed exceptionally_ is also considered _completed_.


```
CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("exception"));
assertTrue(future.isDone());
assertTrue(future.isCompletedExceptionally());
assertFalse(future.isCancelled());
```


The `isCancelled` method returns `true` because the future is canceled. Note that a _canceled_ future is also considered _completed exceptionally_ and _completed_.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
assertFalse(future.isDone());
assertFalse(future.isCompletedExceptionally());
assertFalse(future.isCancelled());
future.cancel(true);
assertTrue(future.isDone());
assertTrue(future.isCompletedExceptionally());
assertTrue(future.isCancelled());
```



### Methods to complete futures

The `CompletableFuture` class has methods to complete futures - to transfer incomplete futures to one of the completion states: normal completion, exceptional completion, and cancellation.

Summary of methods to complete futures


<table>
  <tr>
   <td>Future action
   </td>
   <td>Method behavior
   </td>
   <td>Method name
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td rowspan="3" >complete normally
   </td>
   <td>synchronous
   </td>
   <td><code>complete(value)</code>
   </td>
   <td>completes this <code>CompletableFuture</code> with the given value if not already completed
   </td>
  </tr>
  <tr>
   <td rowspan="3" >asynchronous
   </td>
   <td><code>completeAsync(supplier)​</code>
   </td>
   <td>completes this <code>CompletableFuture</code> with the result of the given `Supplier`
   </td>
  </tr>
  <tr>
   <td><code>completeOnTimeout(value, timeout, timeUnit)</code>
   </td>
   <td>completes this <code>CompletableFuture</code> with the given value if not already completed before the given timeout
   </td>
  </tr>
  <tr>
   <td>complete normally or exceptionally depends on timeout
   </td>
   <td><code>orTimeout(timeout, timeUnit)</code>
   </td>
   <td>exceptionally completes this <code>CompletableFuture</code> with a <code>TimeoutException</code> if not already completed before the given timeout
   </td>
  </tr>
  <tr>
   <td rowspan="2" >complete exceptionally
   </td>
   <td rowspan="2" >synchronous
   </td>
   <td><code>completeExceptionally(throwable)</code>
   </td>
   <td>completes this <code>CompletableFuture</code> with the given exception if not already completed
   </td>
  </tr>
  <tr>
   <td><code>cancel(mayInterruptIfRunning)</code>
   </td>
   <td>completes this <code>CompletableFuture</code> with a <code>CancellationException</code>, if not already completed 
   </td>
  </tr>
</table>


The `cancel(boolean mayInterruptIfRunning)` method has a special implementation feature in the `CompletableFuture` class. The parameter `mayInterruptIfRunning` has no effect because thread interrupts are not used to control processing in this implementation of the `Future` interface. When the `cancel` method is called, the computation is canceled with the `CancellationException`, but the `Thread.interrupt()` method is not called to interrupt the underlying thread.


#### Code examples

The `complete​` method completes the future normally because it has not yet completed.


```
CompletableFuture<String> future = new CompletableFuture<>();
assertFalse(future.isDone());
boolean hasCompleted = future.complete("value");
assertTrue(hasCompleted);
assertTrue(future.isDone());
assertEquals("value", future.get());
```


The `completeAsync` method asynchronously completes the future normally with the result of the given Supplier.


```
CompletableFuture<String> future1 = new CompletableFuture<>();
assertFalse(future1.isDone());
CompletableFuture<String> future2 = future1.completeAsync(() -> "value");
sleep(1);
assertTrue(future2.isDone());
assertEquals("value", future2.get());
```


The `completeOnTimeout​` method completes the future normally with the fallback value because it has not yet completed before the given timeout.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
       .completeOnTimeout("fallback", 1, TimeUnit.SECONDS);
assertEquals("fallback", future.get());
```


The `orTimeout​` method completes the future exceptionally because it has not yet completed before the given timeout.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
       .orTimeout(1, TimeUnit.SECONDS);
future.get(); // throws ExecutionException caused by TimeoutException
```


The `completeExceptionally​` method completes the future exceptionally because it has not yet completed.


```
CompletableFuture<String> future = new CompletableFuture<>();
assertFalse(future.isDone());
assertFalse(future.isCompletedExceptionally());
boolean hasCompleted = future.completeExceptionally(new RuntimeException("exception"));
assertTrue(hasCompleted);
assertTrue(future.isDone());
assertTrue(future.isCompletedExceptionally());
```


The `cancel​` method cancels the future (completes it exceptionally) because it has not yet completed.


```
CompletableFuture<String> future = new CompletableFuture<>();
assertFalse(future.isDone());
assertFalse(future.isCancelled());
boolean isCanceled = future.cancel(false);
assertTrue(isCanceled);
assertTrue(future.isDone());
assertTrue(future.isCancelled());
future.get(); // throws CancellationException
```



### Methods to read futures

The `CompletableFuture` class has methods to read futures, waiting if necessary. Note that in most cases these methods should be used as the last computation step, do not use them inside a computation pipeline.

Summary of methods to read futures


<table>
  <tr>
   <td>Method behavior
   </td>
   <td>Thrown exceptions
   </td>
   <td>Method name
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td rowspan="2" >blocking
   </td>
   <td>throws checked and unchecked exceptions
   </td>
   <td><code>get()</code>
   </td>
   <td>returns the result value when complete (waits if necessary) or throws an exception if completed exceptionally 
   </td>
  </tr>
  <tr>
   <td>throws <em>only</em> unchecked exceptions 
   </td>
   <td><code>join()</code>
   </td>
   <td>returns the result value when complete (waits if necessary) or throws an <em>unchecked</em> if completed exceptionally 
   </td>
  </tr>
  <tr>
   <td>time-blocking
   </td>
   <td>throws checked and unchecked exceptions
   </td>
   <td><code>get(timeout, timeUnit)</code>
   </td>
   <td>returns the result value when complete (waits for at most the given time) or throws an exception if completed exceptionally
   </td>
  </tr>
  <tr>
   <td>non-blocking
   </td>
   <td>throws <em>only</em> unchecked exceptions 
   </td>
   <td><code>getNow(valueIfAbsent)</code>
   </td>
   <td>returns the result value (or throws an <em>unchecked</em> exception if completed exceptionally) if completed, else returns the given <em>valueIfAbsent</em>
   </td>
  </tr>
</table>



#### Code examples

The `get` method waits until the future is completed and returns the result. This method can throw the checked `InterruptedException`, `ExecutionException` exceptions.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
assertEquals("value", future.get());
```


The `join` method waits until the future is completed and returns the result. This method can not throw checked exceptions (it can be used as a method reference for example in Streams API).


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
assertEquals("value", future.join());
```


The `get(timeout, timeUnit)` method waits for at most the given time and throws the checked `TimeoutException` exception because the timeout occurs earlier than the future is completed.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"));
future.get(1, TimeUnit.SECONDS); // throws TimeoutException
```


The `getNow` method does not wait and immediately returns the fallback value because the future has yet not completed. Note that this method does not cause the `CompletableFuture` to complete.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
assertEquals("fallback", future.getNow("fallback"));
assertFalse(future.isDone());
```



### Methods for bulk future operations

The `CompletableFuture` class has two static methods for waiting for _all_ or _any_ of many `CompletableFuture`s to complete, not just two of them.

Summary of methods for bulk futures operations


<table>
  <tr>
   <td>Analog
   </td>
   <td>Method name
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td>similar to <code>runAfterBoth</code>
   </td>
   <td><code>allOf(completableFuture..)</code>
   </td>
   <td>returns a new <code>CompletableFuture&lt;Void></code> that is completed when <em>all</em> of the given <code>CompletableFuture</code>s complete
   </td>
  </tr>
  <tr>
   <td>similar to <code>applyToEither</code>
   </td>
   <td><code>anyOf(completableFuture..)</code>
   </td>
   <td>returns a new <code>CompletableFuture&lt;Object></code> that is completed when <em>any</em> of the given <code>CompletableFuture</code>s complete, with the same result
   </td>
  </tr>
</table>


>Note that all futures can be of different generic types - the methods have arguments of type `CompletableFuture&lt;?>`.


#### Code examples

The static `allOf` method returns a new incomplete future that is completed when _all_ of the given `CompletableFuture`s complete. Note that the result of this method is `CompletableFuture&lt;Void>` and you should get results of each given future by reading them individually.


```
CompletableFuture<?>[] futures = new CompletableFuture<?>[]{
       supplyAsync(() -> sleepAndGet(1, "parallel1")),
       supplyAsync(() -> sleepAndGet(2, "parallel2")),
       supplyAsync(() -> sleepAndGet(3, "parallel3"))
};

CompletableFuture<Void> future = CompletableFuture.allOf(futures);
future.get();

String result = Stream.of(futures)
       .map(CompletableFuture::join)
       .map(Object::toString)
       .collect(Collectors.joining(" "));

assertEquals("parallel1 parallel2 parallel3", result);
```


The static `anyOf` method returns a new incomplete future that is completed when _any_ of the given `CompletableFuture`s complete. Note that the result of this method is `CompletableFuture&lt;Object>` and you should cast the result to the required type manually.


```
CompletableFuture<Object> future = CompletableFuture.anyOf(
       supplyAsync(() -> sleepAndGet(1, "parallel1")),
       supplyAsync(() -> sleepAndGet(2, "parallel2")),
       supplyAsync(() -> sleepAndGet(3, "parallel3"))
);

assertEquals("parallel1", future.get());
```



## Conclusion

The CompletableFuture API is a high-level API that allows building _fluent_ concurrent code. The API is not easy and it is worth studying if you want to write _efficient_ concurrent code.

There are the following rules of thumb in using CompletableFuture API:



*   Know what executor service executes what computation, do not allow a high-priority thread to execute long-running low-priority tasks
*   Avoid blocking methods inside a computation pipeline
*   Avoid short (hundreds of milliseconds) asynchronous computations because frequent context switching can introduce significant overhead
*   Be aware of new exception handling and recovery design that works differently than the `try-catch-finally` statement
*   Manage timeouts to do not wait too long (perhaps indefinitely) for a stuck computation

The CompletableFuture API is rather complicated and is justified to use when the single result depends on many steps that form a complicated _Directed Acyclic Graph_ with parallel and sequential fragments. Try to use more simple tools - Parallel Streams or `ExecutorService`s. Be aware of the drawbacks of asynchronous programming - the asynchronous code is often much more difficult to implement, understand, and debug. Make sure that the CompletableFuture API is an appropriate tool for your job. 
