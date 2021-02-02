# Java parallelism: CompletionStage and CompletableFuture


## Introduction

The `CompletionStage` and `CompletableFuture`classes are high-level parallel Java API that was added in Java 8 to pipeline multiple asynchronous tasks into a single result without the mess of nested callbacks (‘callback hell’). These classes are implementations of the Future/Promise design pattern in Java.

Before adding these classes Java already had much simpler classes to manage asynchronous tasks - in Java 5 were added the `Future` interface and its base implementation the `FutureTaks` class. 

The `Future` interface represents the _result_ of an asynchronous task that has a few methods:



*   to check whether the task is completed or canceled
*   to cancel the task
*   to wait if necessary for the task to complete, and then to retrieve its result

Basically, the `Future` interface represents an incompleted result of submitting a task (either ’Runnable’ or ’Callable’) for asynchronous execution to an ’ExecutorService’ instance. The incompleted result has the blocking ’get’ method and the non-blocking ’isDone’ method to wait for the completion of the task and retrieving the result.

But the the `Future` interface has significant limitations in building multi-step asynchronous tasks: 



*   it’s impossible to register a callback on task competition - the only possible way is to check the result of the `isDone` method in a cycle (to busy-wait)
*   it’s impossible to combine the results of two tasks in a non-blocking way
*   it’s impossible to manually complete a task either with a result or with an exception

To change this, in Java 8 were added (and in Java 9 and Java 12 were updated) the `CompletionStage` interface and its base implementation the `CompletableFuture` class. These classes allow building effective multi-step tasks of a single result - not only in simple linear sequences of task steps but also when the task steps form dependencies as complicated as directed acyclic graphs.


## Futures and promises

The concept of a _future/promise_ exists in many programming languages (JavaScript: `Promise`; Java: `Future`, `CompletionStage`, `CompletableFuture`, Google Guava [ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained); Scala: `scala.concurrent.Future`) that allows writing asynchronous code that still has a _fluent interface_ as synchronous code.

A _future/promise_ represents the eventual result of an asynchronous operation. A _future/promise_ can be in two states: incompleted and completed, and completion can be performed either with a result to indicate success, or with an exception to indicate failure.

In a broader sense, the terms _future_ and _promise_ are used interchangeably, as a placeholder for a parallel-running task that hasn't been completed yet but is expected in the future.

In a narrower sense, a _future_ is a read-only placeholder view of a result (that is yet unavailable), while a _promise_ is a writable, single-assignment object that sets the value of the _future_. 

The following workflow example can help you to understand the idea of future/promise. A consumer sends a long-running task to a producer to be executed asynchronously. The producer creates a promise when it starts that task and sends a future to a consumer. The consumer receives the future that isn’t completed yet and waits for its completion. During waiting, the consumer isn’t blocked and can execute other tasks. When the producer completes the task, it fulfills the promise and thereby provides the future's value. Essentially the promise represents the producing end of the future/promise relationship, while the future represents the consuming end. This explains why promises are single write-only and futures are multiple read-only.

![Future and Promise](/images/future_and_promise.png)

The most important part of futures/promises is the possibility to define a pipeline of operations to be invoked upon completion of the task represented by the future/promise. In comparison with _asynchronous callbacks_, this allows writing more fluent code that supports the composition of nested result/error handlers without ‘callback hell’. In comparison with _blocked code_, this allows implementing quicker asynchronous pipelines without boilerplate synchronization.

In Java, the `Future` interface represents a _future_: it has methods to check if the task is complete, to wait for its completion, and to retrieve the result of the task when it is complete. (The `FutureTask` class has the `void set(V v)` method that it is `protected`). The `CompletableFuture` class can be thought of as a _promise_ since their value can be explicitly set by the `complete` and `completeExceptionally` methods. However, `CompletableFuture` also implements the `Future` interface allowing it to be used as a _future_ as well. 


## Example

The following code example can help you to understand the usage of the `CompletableFuture` class as an implementation of future/promise in Java. In the given workflow it’s necessary to calculate the total price in the USD of two products (the first is priced in the GBP and the second is priced in the EUR), including tax. To do this, it’s necessary to execute the following tasks:



1. to get the price of the first product in the GBP (a long-running call)
2. to get the GBP/USD exchange rate (a long-running call)
3. to calculate the price of the first product in the USD (depends on tasks 1, 2)
4. to get the price of the second product in the EUR (a long-running call)
5. to get the EUR/USD exchange rate (a long-running call)
6. to calculate the price of the second product in the USD (depends on tasks 4, 5)
7. to calculate the price of both product in the USD before tax (depends on tasks 3, 6)
8. to get the value of the tax (a long-running call, depends on tasks 7)
9. to calculate the price of both product in the USD after tax (depends on tasks 7, 8)

Notice that some tasks are long-running (for example, they make remote calls), so it’s worth executing them asynchronously. Also, some tasks here depend on other tasks (they must be executed sequentially) but some are mutually independent (they can be executed parallelly).

The proposed workflow is implemented below in three styles: synchronous, asynchronous `Future`-based, and asynchronous `CompletableFuture`-based.

1) The advantage of the synchronous solution is the simplest code. The disadvantage of this solution is that all tasks are performed sequentially and take the most time, although some of them are independent and can be performed in parallel.


```
int amountInUsd1 = getPriceInGbp() * getExchangeRateGbpToUsd(); //blocking
int amountInUsd2 = getPriceInEur() * getExchangeRateEurToUsd(); //blocking
int amountInUsd = amountInUsd1 + amountInUsd2;
float amountInUsdAfterTax = amountInUsd * (1 + getTax(amountInUsd)); //blocking
```


2) The advantage of an asynchronous `Future`-based solution is that some tasks run in parallel, which saves time. The disadvantage of this solution is that the `Future` interface does not have methods for pipelining tasks - passing the results of some tasks as parameters to other tasks without blocking. As a result, the code of dissolution is the most complicated.


```
Future<Integer> priceInGbp = executorService.submit(this::getPriceInGbp);
Future<Integer> exchangeRateGbpToUsd = executorService.submit(this::getExchangeRateGbpToUsd);
Future<Integer> priceInEur = executorService.submit(this::getPriceInEur);
Future<Integer> exchangeRateEurToUsd = executorService.submit(this::getExchangeRateEurToUsd);

while (!priceInGbp.isDone() || !exchangeRateGbpToUsd.isDone()
       || !priceInEur.isDone() || !exchangeRateEurToUsd.isDone()) {
   Thread.sleep(100); // busy-waiting
}

int amountInUsd1 = priceInGbp.get() * exchangeRateGbpToUsd.get();
int amountInUsd2 = priceInEur.get() * exchangeRateEurToUsd.get();
int amountInUsd = amountInUsd1 + amountInUsd2;

Future<Float> tax = executorService.submit(() -> getTax(amountInUsd));

while (!tax.isDone()) {
   Thread.sleep(100); // busy-waiting
}

float amountInUsdAfterTax = amountInUsd * (1 + tax.get());
```


3) The advantage of the asynchronous `CompletableFuture`-based solution is that the independent tasks are executed in parallel, and the dependent tasks are pipelined using a fluent interface. The disadvantage of this solution is that the `CompletableFuture` class has a rather complex API (35 public methods in the `CompletableFuture` class and 42 inherited methods from the `CompletionStage` interface) - a problem this article is trying to explain.


```
CompletableFuture<Integer> priceInGbp = supplyAsync(this::getPriceInGbp);
CompletableFuture<Integer> exchangeRateGbpToUsd = supplyAsync(this::getExchangeRateGbpToUsd);
CompletableFuture<Integer> priceInEur = supplyAsync(this::getPriceInEur);
CompletableFuture<Integer> exchangeRateEurToUsd = supplyAsync(this::getExchangeRateEurToUsd);

CompletableFuture<Integer> amountInUsd1 = priceInGbp
       .thenCombine(exchangeRateGbpToUsd, (price, exchangeRate) -> price * exchangeRate);
CompletableFuture<Integer> amountInUsd2 = priceInEur
       .thenCombine(exchangeRateEurToUsd, (price, exchangeRate) -> price * exchangeRate);

float amountInUsdAfterTax = amountInUsd1
       .thenCombine(amountInUsd2, (amount1, amount2) -> amount1 + amount2)
       .thenCompose(amountInUsd -> supplyAsync(() -> amountInUsd * (1 + getTax(amountInUsd))))
       .get(); // blocking
```





## The [CompletionStage](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/util/concurrent/CompletionStage.html) interface

The `CompletionStage` interface represents a step in a multi-step future/promise computation. The design of this class has three considerations _how_ stages can be pipelined:



*   stages can be executed synchronously or asynchronously
*   a stage can either compute a value (returns a single result) or performs an action (returns no result)
*   a stage can be initiated by finishing one or two previous stages and can initiate a subsequent stage

The absolute majority of the methods (except one) of the `CompletionStage` return another `CompletionStage` object that allows steps pipelining. Note that during chaining, methods are not immediately executed, but are lazily registered for further executions like callbacks. The methods are actually executed when the given future/promise completes.

Methods of the `CompletionStage` interface can be divided into two groups by their purpose:



*   methods to pipeline computations
*   methods to handle exceptions

The `CompletionStage` interface doesn’t contain methods for stage creation nor stage status checking nor stage completion. This functionality is delegated to `CompletionStage` implementations - mainly to the `CompletableFuture` class.


### Methods to pipeline computations

The `CompletionStage` interface has 43 methods (14 groups of 3 similarly overloaded methods in each plus 1 method) which at first glance may seem confusing. In fact, the methods have three distinguished naming patterns. 

The _first_ naming pattern explains how a new stage is initiated:



*   if a method’s name has the `then` prefix, then the new stage is initiated after completion of a single previous stage
*   if a method’s name has the `either` suffix, then the new stage is initiated after completion of the first of two previous stages
*   if a method’s name has the `both` suffix, then the new stage is initiated after completion of both previous stages

The _second_ naming pattern explains what computations perform the new stage:



*   if a method’s name has the `apply` fragment, then the new stage applies an argument by a `Function` (takes argument(s) and returns one result)
*   if a method’s name has the `accept` fragment, then the new stage accepts an argument by a `Consumer` (takes argument(s) and returns no result)
*   if a method’s name has the `run` fragment, then the new stage runs an action by a `Runnable` (takes no argument and returns no result)

>If the new stage depends on both previous stages (has two arguments), it uses `BiFunction` instead of `Function` and `BiConsumer` instead of `Consumer`.

Summary of the methods:


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
   <td>thenCombine*
   </td>
   <td>
   </td>
   <td>thenAcceptBoth
   </td>
   <td>runAfterBoth
   </td>
  </tr>
</table>


>Note that the `thenCombine` method could have been named `thenApplyBoth`.

The mathods that have Function, BiFunction arguments return `CompletionStage&lt;T>` that can be used to pass values. The methods that have Consumer, BiConsumer, Runnable return `CompletionStage&lt;Void>` that can be used to perform computations with side-effects and can signalize the fact of completion of computation either with result or with an exception.

The _third_ naming pattern explains what thread executes the new stage:



*   if a method has a signature like `something(...)` then the new stage is executed by _the default facility_ (that can be synchronous or asynchronous).
*   if a method has a signature like `somethingAsync(...)` then the new stage is executed by _the default asynchronous facility_.
*   if a method has a signature like `somethingAsync(..., Executor)` then the new stage is executed by the supplied `Executor`.

If stages are independent and so can be executed in parallel, the asynchronous execution can give a significant performance improvement (of course if you have enough processor cores). However, if tasks are short (hundreds of milliseconds), then context switching between threads can introduce significant overhead.

Note that _the default facility_ and _the default asynchronous facility_ are specified by `CompletionStage` implementations, not by the interface itself. Looking ahead, the `CompletableFuture` implementation of the `CompletionStage` interface uses the thread that completes the stage as _the default facility_ and `ForkJoinPool.commonPool()` as _the default asynchronous facility_.


### Methods to handle exceptions

Synchronous computation can be completed normally or exceptionally. To recover from these exceptions, it’s possible to use a `try-catch` statement. Asynchronous computation can be completed normally or exceptionally as well. But because the pipelined stages can be executed in different threads, it’s impossible to use a `try-catch` statement in one thread to catch an exception thrown from another thread. 

The `CompletionStage` interface has special specifications to handle exceptions. Each stage has two completion types of equal importance, the normal completion, and the exceptional completion. If a stage completes normally, the dependent stages start executing. If a stage completes exceptionally, all dependent stages complete exceptionally as well, unless there is an exception recovery method in the pipeline.

If it’s necessary to perform some action, when the previous stage completes either normally or exceptionally, then the method  `whenComplete` should be used. A `BiConsumer` argument of the `whenComplete`method is called when the previous stage completes either normally or exceptionally and allows to read both the result and the exception, but doesn’t allow to modify them.

If it’s necessary to recover from an exception (to replace the exception with some fallback value), then the `handle` and `exceptionally` methods should be used. A `BiFunction` argument of the `handle` method is called when the previous stage completes either normally or exceptionally. A `Function` argument of the `exceptionally` method is called when the previous stage completes exceptionally. In both cases, an exception is not propagated to the next stage.

The `exceptionallyCompose` method is similar to the `exceptionally` method - its `Function` argument of the `exceptionally` method is called when the previous stage completes exceptionally. The difference is that the result of the `Function` is a subclass of `CompletionStage`, which is useful when exception recovery is a long operation that is reasonable to execute as a separate stage (possible asynchronously).

>The difference between the `exceptionally` and `exceptionallyCompose` methods are the same as the difference between the `thenApply` and `thenCompose` methods - the latest has the result of the `Function` is a subclass of `CompletionStage`.

Summary of the methods:


<table>
  <tr>
   <td>
   </td>
   <td>
   </td>
   <td>Method
   </td>
  </tr>
  <tr>
   <td>can’t modify result
   </td>
   <td rowspan="2" >called on success or exception
   </td>
   <td>whenComplete
   </td>
  </tr>
  <tr>
   <td rowspan="3" >can modify result
   </td>
   <td>handle
   </td>
  </tr>
  <tr>
   <td rowspan="2" >called on exception
   </td>
   <td>exceptionally 
   </td>
  </tr>
  <tr>
   <td>exceptionallyCompose 
   </td>
  </tr>
</table>
