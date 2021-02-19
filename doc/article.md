# Asynchronous programming in Java with CompletableFuture


## Introduction

The CompletableFuture API is a high-level API for asynchronous programming in Java. This API supports _pipelining_ (also known as _chaining_ or _combining_) of multiple asynchronous and synchronous computations into a single result without the mess of nested callbacks (“callback hell“). This API also is an implementation of the _future/promise_ high-level concurrency constructs in Java.

Since Java 5 there is a much simpler API for asynchronous programming: the `Future` interface and its base implementation, the `FutureTask` class. The `Future` interface represents the result of an asynchronous computation and has only a few methods:



*   to check if a task is completed or canceled
*   to cancel a task
*   to wait for a task to complete (if necessary) and then to get its result

However, the `Future` interface has significant limitations in building non-trivial asynchronous computations: 



*   it is impossible to register a callback for a future competition
*   it is impossible to pipeline futures in a non-blocking way
*   it is impossible to manually complete a future

To overcome these limitations, Java 8 added (and Java 9 and Java 12 updated) the `CompletionStage` interface and its base implementation, the `CompletableFuture` class. These classes allow building efficient and fluid multi-stage asynchronous computation, where stages can be forked, chained, and joined.

However, the CompletableFuture API is not simple. The `CompletionStage` interface has 43 public methods. The `CompletableFuture` class implements 5 methods from the `Future` interface, 43 methods from the `CompletionStage` interface, and implements 30 of its public methods.


## Futures and promises

Future/promise is a high-level concurrency construct that decouples a value (a future) from how it was computed (a promise). That allows writing more fluent concurrent programs that transfer objects between threads without using any of the explicit synchronization mechanisms for _shared mutable objects_ like mutexes. The futures/promises mechanism works very well when there are multiple threads working on different tasks and the results need to be combined together by the main thread. 

Implementations of future/promise exist in many programming languages:



*   JavaScript: `Promise`
*   Java: `java.util.concurrent.Future`, `java.util.concurrent.CompletableFuture`
*   Scala: `scala.concurrent.Future`
*   C#: `Task`, `TaskCompletionSource`

Concepts of futures and promises are often used interchangeably, but in reality, they are separate objects that encapsulate the two different sets of functionality. 

A _future_ is a read-only object to encapsulate a value that may not be available yet but will be provided at some point. The future is used by a consumer to retrieve the result which was computed. A _promise_ is a writable, single-assignment object to guarantee that some task will compute some result and make it available in the corresponding future object. The promise is used by a producer in order to store the success value or exception in the corresponding future. 

This is why a method for reading a value only exists in the future, and a method for writing a value only exists in a promise.

The following workflow example can help you to understand the idea of future/promise. A consumer sends a task to a producer to execute it asynchronously. The producer creates a promise that starts the given task. From the promise, the producer extracts the future and sends it to the consumer. The consumer receives the future that is not completed and waits for its completion. While waiting for future completion, the consumer can use blocking and non-blocking methods.

The consumer can call the _getter_ method of the future object to wait for the data in the future to be available. if the future is already available, the call to the _getter_ method will return the result immediately. Otherwise, the call to the _getter_ method will wait until the future is finished. 

Also, the consumer can use a non-blocking _checking_ method that verifies whether the future has already completed. If the future is not yet available, the call to the _checking_ method returns immediately and the consumer can execute other tasks. Otherwise, the consumer calls the _getter_ method that returns the result immediately.

Once the task has finished, the producer sets the value of the promise and the future becomes available. But when the task fails, the future will contain an exception instead of a success value. So when the consumer calls the _getter_ method, the exception in the future will be thrown. 

![future and promise workflow](/images/future_and_promise_workflow.png)

One of the important features of a future/promise implementation is the ability to chain futures together. The idea is that when one future/promise is ready another future/promise is created that accepts the result of the previous one. This means the consumer is not blocked by calling the getter method on a future and in fact once the future becomes completed the result of the previous task is automatically passed to the next task in the chain. In comparison with callbacks, this allows writing more fluent asynchronous code that supports the composition of nested success and failure handlers without ”callback hell”.

In Java, the `Future` interface represents a _future_: it has the `isDone` method to check if the task is complete, the `get` method to wait for its completion, and get the result of the task when it is complete. The `CompletableFuture` class represents a _promise_: it has the `complete` and `completeExceptionally` methods to sets the value with a successful result or with an exception. However, the `CompletableFuture` class also implements the `Future` interface allowing it to be used as a _future_ as well. 

![Java future and promise class diagram](/images/Java_future_and_promise_class_diagram.png)


## CompletableFuture in practice

The following code example can help you to understand the usage of the `CompletableFuture` class as an implementation of future/promise in Java.

In the given workflow it is necessary to calculate the net price in the USD of a product (that is priced in the EUR), calculate a tax and the gross price. To do this, it is necessary to execute the following tasks:



1. to get the price of the product in the EUR (a long-running task)
2. to get the EUR/USD exchange rate (a long-running task)
3. to calculate the net price of the product in the USD (depends on tasks 1, 2)
4. to get the value of the tax (a long-running task, depends on tasks 3)
5. to calculate the gross price of the product in the USD (depends on tasks 3, 4)

Notice that some tasks are long-running (for example, they make remote calls), so it is worth executing them asynchronously. Also, some tasks depend on other tasks (they have to be executed sequentially) but some are independent (they can be executed in parallel).

The proposed workflow is implemented below in three styles: synchronous, asynchronous `Future`-based and asynchronous `CompletableFuture`-based.

>In _synchronous_ programming, the main thread starts an axillary task and blocks until this task is finished. When the axillary task is completed, the main thread continues the main task.

>In _asynchronous_ programming, the main thread starts an axillary task in a worker thread and continues its task. When the worker thread finished the auxiliary task, it notified the main thread (for example) by a callback.

1) The advantage of the synchronous solution is the simplest code. The disadvantage of this solution is that all tasks execute sequentially and take the most time (although some of them are independent and can be performed in parallel).


```
logger.info("this task started");

int netAmountInUsd = getPriceInEur() * getExchangeRateEurToUsd(); // blocking
float grossAmountInUsd = netAmountInUsd * (1 + getTax(netAmountInUsd)); // blocking

logger.info("this task finished: {}", grossAmountInUsd);
logger.info("another task started");
```


2) The advantage of an asynchronous `Future`-based solution is that some tasks run in parallel, which saves time. The disadvantage of this solution is that the `Future` interface does not have methods for pipelining tasks  (passing the results of some tasks as parameters to other tasks) without blocking. Despite this implementation allows two tasks to run in parallel, the code of this solution is the most complicated.


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


3) The advantage of the asynchronous `CompletableFuture`-based solution is that the independent tasks are executed in parallel, and the dependent tasks are pipelined using a fluent interface. The disadvantage of this solution is that the parallel code may be difficult to understand.


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

The `CompletionStage` interface represents a stage in a multi-stage (possibly asynchronous) computation. The design of this class has three considerations _how_ stages can be pipelined:



*   stages can be executed synchronously or asynchronously
*   a stage can either compute a value (returns a single result) or performs an action (returns no result)
*   a stage can be started by finishing one or two previous stages and can start a single following stage

Methods of the `CompletionStage` interface can be divided into two groups by their purpose:



*   methods to pipeline computations
*   methods to handle exceptions

![methods of CompletionStage](/images/methods_of_CompletionStage.png)

The `CompletionStage` interface contains methods for stages pipelining but not for other parts of their workflow (creation, checking, completion, retrieving). This functionality is delegated to `CompletionStage` implementations - mainly to the `CompletableFuture` class.


### Methods to pipeline computations

The `CompletionStage` interface has 43 public methods that form three distinguished naming patterns.

The _first_ naming pattern explains _how_ a new stage is started:



*   if a method name has fragment `then`, then the new stage is started after completion of a single previous stage
*   if a method name has fragment `either`, then the new stage is started after completion of the first of two previous stages previous stages
*   if a method name has fragment `both`, then the new stage is started after completion of both of two previous stages

The _second_ naming pattern explains _what computations perform_ the new stage:



*   if a method name has fragment `apply`, then the new stage transforms an argument by a `Function` (takes argument(s) and returns one result)
*   if a method name has fragment `accept`, then the new stage accepts an argument by a `Consumer` (takes argument(s) and returns no result)
*   if a method name has fragment `run`, then the new stage runs an action by a `Runnable` (takes no argument and returns no result)

>If the new stage depends on both previous stages, it uses `BiFunction` instead of `Function` and `BiConsumer` instead of `Consumer`.

Summary of the methods to pipeline computations:


<table>
  <tr>
   <td rowspan="2" >
   </td>
   <td colspan="2" >takes argument(s) and 
<p>
returns a result
   </td>
   <td colspan="2" >takes argument(s) and 
<p>
returns no result
   </td>
   <td>takes no argument and 
<p>
returns no result
   </td>
  </tr>
  <tr>
   <td>Function
   </td>
   <td>BiFunction
   </td>
   <td>Consumer
   </td>
   <td>BiConsumer
   </td>
   <td>Runnable
   </td>
  </tr>
  <tr>
   <td>then
   </td>
   <td>thenApply, thenCompose
   </td>
   <td>
   </td>
   <td>thenAccept
   </td>
   <td>
   </td>
   <td>thenRun
   </td>
  </tr>
  <tr>
   <td>either
   </td>
   <td>applyToEither
   </td>
   <td>
   </td>
   <td>acceptEither
   </td>
   <td>
   </td>
   <td>runAfterEither
   </td>
  </tr>
  <tr>
   <td>both
   </td>
   <td>
   </td>
   <td>thenCombine
   </td>
   <td>
   </td>
   <td>thenAcceptBoth
   </td>
   <td>runAfterBoth
   </td>
  </tr>
</table>


The methods that have `Function`, `BiFunction` arguments return `CompletionStage&lt;T>` that can be used to pass values. The methods that have `Consumer`, `BiConsumer`, `Runnable` return `CompletionStage&lt;Void>` that can be used to perform computations with _side-effects_ and can signalize the fact of completion of computation either with the result or with an exception.

The _third_ naming pattern explains _what thread executes_ the new stage:



*   if a method has fragment `something(...)`, then the new stage is executed by _the default facility_ (that can be synchronous or asynchronous).
*   if a method has fragment `somethingAsync(...)`, then the new stage is executed by _the default asynchronous facility_.
*   if a method has fragment `somethingAsync(..., Executor)`, then the new stage is executed by the supplied `Executor`.

Note that _the default facility_ and _the default asynchronous facility_ are specified by `CompletionStage` implementations, not by the interface itself. Looking ahead, the `CompletableFuture` implementation of the `CompletionStage` interface uses the thread that completes the stage as _the default facility_ and `ForkJoinPool.commonPool()` as _the default asynchronous facility_.

>ForkJoinPool.commonPool() is shared across the JVM, it is implicitly used by the `CompletableFuture` class and Parallel Streams.


##### Code examples

The `thenApply` method creates a new stage, that upon completion transforms the given `Function` to the result of the single previous stage.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("single"));

CompletionStage<String> stage = stage1.thenApply(
       s -> s.toUpperCase());

assertEquals("SINGLE", stage.toCompletableFuture().get());
```


The `thenCompose` method creates a new stage, that upon completion transforms the given `Function` to the result of the single previous stage. This method is similar to the `thenApply` method described above. The difference is that the result of the `Function` is a subclass of `CompletionStage`, which is useful when a functional transformation is a long operation that is reasonable to execute as a separate stage (possible asynchronously).


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("sequential1"));

CompletionStage<String> stage = stage1.thenCompose(
       s -> {
           CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet((s + " " + "sequential2").toUpperCase()));
           return stage2;
       });

assertEquals("SEQUENTIAL1 SEQUENTIAL2", stage.toCompletableFuture().get());
```


>You should use the `thenApply` method if you want to transform one `CompletionStage`s with a _fast_ function.

>You should use the `thenCompose` method if you want to transform one `CompletionStage`s with a _slow_ function.

The `applyToEither` method creates a new stage, that upon completion transforms the given `Function` to the result of this stage _or_ another stage (which completes first).


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

CompletionStage<String> stage = stage1.applyToEither(stage2,
       s -> s.toUpperCase());

assertEquals("PARALLEL1", stage.toCompletableFuture().get());
```


The `thenCombine` method creates a new stage, that upon completion transforms the given `BiFunction` to the results of this stage _and_ another stage. 


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet("parallel2"));

CompletionStage<String> stage = stage1.thenCombine(stage2,
       (s1, s2) -> (s1 + " " + s2).toUpperCase());

assertEquals("PARALLEL1 PARALLEL2", stage.toCompletableFuture().get());
```


>You should use the `thenCombine` method if you want to transform two `CompletionStage`s _in parallel_. 

>You should use the `thenCompose` method if you want to transform two `CompletionStage`s _sequentially_.

The `thenAccept` method creates a new stage, that upon completion consumes the result of this stage to the given `Consumer`.


```
CompletableFuture<String> stage1 = supplyAsync(() -> sleepAndGet("single"));

CompletionStage<Void> stage = stage1.thenAccept(
       s -> logger.info("consumes single: {}", s));

assertNull(stage.toCompletableFuture().get());
```


The `acceptEither` method creates a new stage, that upon completion consumes the result of this stage _or_ another stage the given `Consumer`(which completes first).


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

CompletionStage<Void> stage = stage1.acceptEither(stage2,
       s -> logger.info("consumes first: {}", s));

assertNull(stage.toCompletableFuture().get());
```


The `thenAcceptBoth` method creates a new stage, that upon completion consumes the result of this stage _and_ another stage the given `BiConsumer`.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

CompletionStage<Void> stage = stage1.thenAcceptBoth(stage2,
       (s1, s2) -> logger.info("consumes both: {} {}", s1, s2));

assertNull(stage.toCompletableFuture().get());
```


The `thenRun` method creates a new stage, that upon completion runs the given `Runnable` after the execution of this stage.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet("single"));

CompletionStage<Void> stage = stage1.thenRun(
       () -> logger.info("runs after single"));

assertNull(stage.toCompletableFuture().get());
```


The `runAfterEither` method creates a new stage, that upon completion runs the given `Runnable` after the execution of this stage _or_ other stage.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

CompletionStage<Void> stage = stage1.runAfterEither(stage2,
       () -> logger.info("runs after first"));

assertNull(stage.toCompletableFuture().get());
```


The `runAfterBoth` method creates a new stage, that upon completion runs the given `Runnable` after the execution of this stage _and_ another stage.


```
CompletionStage<String> stage1 = supplyAsync(() -> sleepAndGet(1, "parallel1"));
CompletionStage<String> stage2 = supplyAsync(() -> sleepAndGet(2, "parallel2"));

CompletionStage<Void> stage = stage1.runAfterBoth(stage2,
       () -> logger.info("runs after both"));

assertNull(stage.toCompletableFuture().get());
```



### Methods to handle exceptions

Synchronous computation can be completed normally or exceptionally. To recover from these exceptions, it is possible to use `try-catch-finally` statements. Asynchronous computation can be completed normally or exceptionally as well. However, because the pipelined stages can be executed in different threads, the `CompletionStage` interface has special specifications to handle exceptions. 

Each stage has two completion types of equal importance: the normal completion and the exceptional completion. If a stage completes normally, the dependent stages start executing. If a stage completes exceptionally, all dependent stages complete exceptionally, unless there is an exception recovery method in the pipeline.

Summary of the methods to handle exceptions:


<table>
  <tr>
   <td>
   </td>
   <td>
   </td>
   <td>Method
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td>can not modify the result
   </td>
   <td rowspan="2" >called on success or exception
   </td>
   <td>`whenComplete(biConsumer)`
   </td>
   <td>returns a new `CompletionStage` that upon completion accepts the result or exception of this stage and returns <em>the same result or exception without modifying them</em>
   </td>
  </tr>
  <tr>
   <td rowspan="2" >can modify the result
   </td>
   <td>`handle(biFunction)`
   </td>
   <td>returns a new `CompletionStage` that upon completion accepts the result or exception of this stage and returns <em>the new result or exception</em>
   </td>
  </tr>
  <tr>
   <td>called on exception
   </td>
   <td>`exceptionally(function)`
   </td>
   <td>
   </td>
  </tr>
</table>


If it is necessary to perform some action, when the previous stage completes either normally or exceptionally, then should be used the `whenComplete` method. A `BiConsumer` argument of the `whenComplete` method is called when the previous stage completes either normally or exceptionally and allows to read both the result (or `null` if none) and the exception (or `null` if none), but does not allow to modify them.

If it is necessary to recover from an exception (to replace the exception with some fallback value), then should be used the `handle` and `exceptionally` methods. A `BiFunction` argument of the `handle` method is called when the previous stage completes either normally or exceptionally. A `Function` argument of the `exceptionally` method is called when the previous stage completes exceptionally. In both cases, an exception is not propagated to the next stage.


##### Code examples

The `whenComplete` method accepts a nullable result and an exception but can not modify the return value - it is still completed exceptionally.


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


The `handle` method transforms a nullable result and an exception and converts the result from being completed exceptionally to completed normally.


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


The `exceptionally` method transforms an exception and converts the result from being completed exceptionally to completed normally.


```
CompletableFuture<String> future = CompletableFuture.<String>failedFuture(new RuntimeException("exception"))
       .exceptionally(t -> "failure: " + t.getMessage());
assertTrue(future.isDone());
assertFalse(future.isCompletedExceptionally());
assertEquals("failure: exception", future.get());
```



### The [CompletableFuture](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/CompletableFuture.html) class

The `CompletableFuture` class is the main implementation of the `CompletionStage` interface. However, it also implements the `Future` interface. That means the `CompletableFuture` class can simultaneously represent a step in a multi-step computation and the result of an asynchronous task.

In comparing with the `FutureTask` class the `CompletableFuture` class has significant capabilities: 



*   it is possible to register a callback for future competition
*   it is impossible to pipeline computations in a non-blocking way
*   it is impossible to manually complete a future (either with a result or with an exception)

The general workflow of `CompletableFuture` objects as a future/promise:



1. a _creating_ thread creates an incomplete future and adds computation handlers to it
2. a _reading_ thread waits (in a blocking or non-blocking way) until the future is completed normally or exceptionally
3. a _completing_ thread completes the future and unblocks the _reading _thread

Besides 5 methods implemented from the `Future` interface and 43 methods implemented from the `Future` interface, the `CompletableFuture` class additionally defines 30 public methods. These methods can be divided into five groups by their purpose:



*   methods to create futures
*   methods to verify futures
*   methods to complete futures
*   methods to get results of futures
*   methods for bulk futures operations

![methods of CompletableFuture](/images/methods_of_CompletableFuture.png)


#### Methods to create futures

By the general workflow, a future is created uncompleted in a _creating_ thread, is completed in a _completing_ thread, and gets its value in the _reading_ thread. However, in some cases, one on many of the threads can be the same. For that purpose, the `CompletableFuture` class has a set of methods to create futures in different states:



*   methods to create uncompleted futures (when the creating thread, completing thread, and reading thread are different)
*   methods to create completing futures  (when the same thread creates a future and completes it)
*   methods to create already completed futures (when the same thread creates a future, completes it, and gets its result)

Summary of the methods to create futures


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
   <td>uncompleted
   </td>
   <td>`newIncompleteFuture()`
   </td>
   <td>returns a new incomplete `CompletableFuture`
   </td>
  </tr>
  <tr>
   <td rowspan="2" >asynchronously completing
   </td>
   <td>`supplyAsync(supplier)`
   </td>
   <td>returns a new `CompletableFuture` that is asynchronously completed with the value from the given `Supplier`
   </td>
  </tr>
  <tr>
   <td>`runAsync(runnable)`
   </td>
   <td>returns a new `CompletableFuture&lt;Void>` of that is asynchronously completed after it runs the given `Runnable`
   </td>
  </tr>
  <tr>
   <td rowspan="2" >completed
   </td>
   <td>`completedFuture(value)​`
   </td>
   <td>returns a new `CompletableFuture` that is already completed with the given value
   </td>
  </tr>
  <tr>
   <td>`failedFuture(throwable)`
   </td>
   <td>returns a new `CompletableFuture` that is already completed exceptionally with the given exception
   </td>
  </tr>
</table>


>The no-arg constructor of `CompletableFuture` also creates an incomplete future.


##### Code examples

The no-arg constructor creates an uncompleted future.


```
CompletableFuture<String> future = new CompletableFuture<>();
assertFalse(future.isDone());
```


The factory `newIncompleteFuture` method creates an uncompleted future. This method should be overridden, if you are implementing a subclass of `CompletableFuture`.


```
CompletableFuture<String> future1 = CompletableFuture.completedFuture("value");
assertTrue(future1.isDone());
CompletableFuture<String> future2 = future1.newIncompleteFuture();
assertFalse(future2.isDone());
```


The `supplyAsync` method creates an uncompleted future that is asynchronously completed with the value from the given `Supplier`.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
assertEquals("value", future.get());
```


The `runAsync` method creates an uncompleted future that is asynchronously completed after running the given `Runnable`.


```
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> logger.info("runnable"));
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



#### Methods to verify futures

To verify futures, the `CompletableFuture` class has _non-blocking_ methods to check whether the future is uncompleted or completed normally exceptionally on canceled.

Summary of the methods to verify futures


<table>
  <tr>
   <td>Behavior
   </td>
   <td>Method name
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td rowspan="3" >non-blocking
   </td>
   <td>`isDone`
   </td>
   <td>returns `true` if the `CompletableFuture` is completed in any fashion: normally, exceptionally, or via cancellation
   </td>
  </tr>
  <tr>
   <td>`isCompletedExceptionally`
   </td>
   <td>returns `true` if this `CompletableFuture` is completed exceptionally, including cancellation
   </td>
  </tr>
  <tr>
   <td>`isCancelled`
   </td>
   <td>returns `true` if this `CompletableFuture` is canceled <em>before it completed normally</em>
   </td>
  </tr>
</table>


>It is impossible to cancel an already completed future.


##### Code examples

The `isDone` method returns whether the future is completed.


```
CompletableFuture<String> future = CompletableFuture.completedFuture("value");
assertTrue(future.isDone());
assertFalse(future.isCompletedExceptionally());
assertFalse(future.isCancelled());
assertEquals("value", future.get());
```


The `isCompletedExceptionally` method returns whether the future is completed exceptionally. Note that a future completed exceptionally is also completed.


```
CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("exception"));
assertTrue(future.isDone());
assertTrue(future.isCompletedExceptionally());
assertFalse(future.isCancelled());
```


The `isCancelled` method returns whether the future is canceled. Note that a canceled future is also completed exceptionally and completed.


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



#### Methods to complete futures

The `CompletableFuture` class has methods to complete futures - to transit uncompleted futures in one of the completion states: completed normally, exceptionally on canceled. 

Summary of the methods to complete futures


<table>
  <tr>
   <td>Behavior
   </td>
   <td>
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
   <td>complete(value)
   </td>
   <td>completes this `CompletableFuture` with the given value if not already completed
   </td>
  </tr>
  <tr>
   <td rowspan="3" >asynchronous
   </td>
   <td>completeAsync(supplier)​
   </td>
   <td>completes this `CompletableFuture` with the result of the given `Supplier`
   </td>
  </tr>
  <tr>
   <td>completeOnTimeout(value, timeout, timeUnit)
   </td>
   <td>completes this `CompletableFuture` with the given value if not already completed before the given timeout
   </td>
  </tr>
  <tr>
   <td>complete normally or exceptionally depends on timeout
   </td>
   <td>orTimeout(timeout, timeUnit)
   </td>
   <td>exceptionally completes this `CompletableFuture` with a `TimeoutException` if not already completed before the given timeout
   </td>
  </tr>
  <tr>
   <td rowspan="2" >complete exceptionally
   </td>
   <td rowspan="2" >synchronous
   </td>
   <td>completeExceptionally(throwable)
   </td>
   <td>completes this `CompletableFuture` with the given exception if not already completed
   </td>
  </tr>
  <tr>
   <td>cancel(mayInterruptIfRunning)
   </td>
   <td>completes this `CompletableFuture` with a `CancellationException`, if not already completed 
   </td>
  </tr>
</table>


The `cancel(boolean mayInterruptIfRunning)` method has a special implementation peculiarity in the `CompletableFuture` class. The parameter `mayInterruptIfRunning` does not affect because thread interrupts are not used to control processing in this implementation of the `Future` interface. When the `cancel` method is called, the computation is canceled by the `CancellationException`, but the `Thread.interrupt()` method is not called to interrupt the underlying thread.


##### Code examples

The `complete​` method completes the future normally because it is not already completed.


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


The `completeOnTimeout​` method completes this future normally with the fallback value because it is not already completed before the given timeout.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
       .completeOnTimeout("fallback", 1, TimeUnit.SECONDS);
assertEquals("fallback", future.get());
```


The `orTimeout​` method completes this future exceptionally because it is not already completed before the given timeout.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"))
       .orTimeout(1, TimeUnit.SECONDS);
future.get(); // throws ExecutionException caused by TimeoutException
```


The `completeExceptionally​` method completes this future exceptionally because it is not already completed.


```
CompletableFuture<String> future = new CompletableFuture<>();
assertFalse(future.isDone());
assertFalse(future.isCompletedExceptionally());
boolean hasCompleted = future.completeExceptionally(new RuntimeException("exception"));
assertTrue(hasCompleted);
assertTrue(future.isDone());
assertTrue(future.isCompletedExceptionally());
```


The `cancel​` method cancels the future (completes exceptionally) because it is not already completed.


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



#### Methods to get results of futures

To get the result of a `CompletableFuture`, it is possible to use the following blocking and non-blocking methods. In most cases, these methods should be used as the last computation step, do not use them inside pipelined stages.

Summary of the methods to get results of futures


<table>
  <tr>
   <td>Behavior
   </td>
   <td>
   </td>
   <td>Method name
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td rowspan="2" >blocking
   </td>
   <td>throws checked exceptions
   </td>
   <td>`get()`
   </td>
   <td>returns the result value when complete (waits if necessary) or throws an exception if completed exceptionally 
   </td>
  </tr>
  <tr>
   <td>
   </td>
   <td>`join()`
   </td>
   <td>returns the result value when complete (waits if necessary) or throws an <em>unchecked</em> if completed exceptionally 
   </td>
  </tr>
  <tr>
   <td>time-blocking
   </td>
   <td>throws checked exceptions
   </td>
   <td>`get(timeout, timeUnit)`
   </td>
   <td>returns the result value when complete (waits for at most the given time) or throws an exception if completed exceptionally
   </td>
  </tr>
  <tr>
   <td>non-blocking
   </td>
   <td>
   </td>
   <td>`getNow(valueIfAbsent)`
   </td>
   <td>returns the result value (or throws an <em>unchecked</em> exception if completed exceptionally) if completed, else returns the given <em>valueIfAbsent</em>
   </td>
  </tr>
</table>


>If this `CompletableFuture` is canceled, all these methods throw an unchecked `CancellationException`.


##### Code examples

The `get` method waits until the future is completed and returns the result. This method can throw the checked `InterruptedException`, `ExecutionException` exceptions.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
assertEquals("value", future.get());
```


The `join` method waits until the future is completed and returns the result. This method can throw no checked exceptions, so it can be used as a method reference for example in Java Streams.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
assertEquals("value", future.join());
```


The `get(timeout, timeUnit)` method waits for at most the given time and throws the checked `TimeoutException` exception because the timeout occurs earlier than the future is completed.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet(2, "value"));
future.get(1, TimeUnit.SECONDS); // throws TimeoutException
```


The `getNow` method does not wait and returns the fallback value immediately because the future has yet not been completed. Note that this call does not force the CompletableFuture to complete.


```
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> sleepAndGet("value"));
assertEquals("fallback", future.getNow("fallback"));
assertFalse(future.isDone());
```



#### Methods for bulk operations

The `CompletableFuture` class has two static methods for waiting for _all_ or _any_ of many `CompletableFuture`s to complete, not just two of them.

Summary of the methods for bulk futures operations


<table>
  <tr>
   <td>Similarity
   </td>
   <td>Method name
   </td>
   <td>Method description
   </td>
  </tr>
  <tr>
   <td>similar to `runAfterBoth`
   </td>
   <td>`allOf(completableFuture..)`
   </td>
   <td>returns a new CompletableFuture&lt;Void> that is completed when all of the given `CompletableFuture`s complete
   </td>
  </tr>
  <tr>
   <td>similar to `applyToEither`
   </td>
   <td>`anyOf(completableFuture..)`
   </td>
   <td>returns a new `CompletableFuture&lt;Object>` that is completed when any of the given `CompletableFutures` complete, with the same result
   </td>
  </tr>
</table>


>Note that all futures can be of different generic types: `CompletableFuture&lt;?>`.


##### Code examples

The static `allOf` method returns a new incomplete future that is completed when all of the given `CompletableFuture`s complete. Note that the result of this method is `CompletableFuture&lt;Void>` and you should get results of each given future by inspecting them individually.


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


The static `anyOf` method returns a new incomplete future that is completed when any of the given `CompletableFuture`s complete. Note that the result of this method is `CompletableFuture&lt;Object>` and you should cast the result to the required type manually.


```
CompletableFuture<Object> future = CompletableFuture.anyOf(
       supplyAsync(() -> sleepAndGet(1, "parallel1")),
       supplyAsync(() -> sleepAndGet(2, "parallel2")),
       supplyAsync(() -> sleepAndGet(3, "parallel3"))
);

assertEquals("parallel1", future.get());
```



## Conclusion

The CompletableFuture API is a high-level API that allows building high-level concurrent code. The API is not easy and it is worth studying if you want to write _efficient_ concurrent code.

There are the following rules of thumb in using CompletableFuture API:



*   Know what executor service executes what computation, do not allow a high-priority thread to execute long-running low-priority tasks
*   Avoid blocking methods inside a computation pipeline
*   Avoid short (hundreds of milliseconds) asynchronous computations because frequent context switching can introduce significant overhead
*   Be aware of new exception handling and recovery design that works differently than the `try-catch-finally` statement
*   Manage timeouts to do not wait too long (perhaps indefinitely) for a stuck computation

The CompletableFuture API is rather complicated and is justified to use when the single result depends on many steps that form a complicated _Directed Acyclic Graph_ with parallel and sequential fragments. Try to use more simple tools - Parallel Streams or `ExecutorService`s. Be aware of the drawbacks of asynchronous programming - the asynchronous code is often much more difficult to implement, understand, and debug. Make sure that the CompletableFuture API is an appropriate tool for your job. 
