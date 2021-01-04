# CompletableFuture


## Introduction

In Java 5 was added the `Future` interface and its base implementation the `FutureTaks` class.

The `Future` interface represents the _result_ of an asynchronous computation. It has a few methods:



*   to check whether the computation is completed or canceled
*   to cancel the execution of this task
*   to wait if necessary for the computation to complete, and then to retrieve its result

The `FutureTask` class implements both `Future` and `Runnable` interfaces. From the first interface, the class inherited the ’isDone’ method to check if the computation is complete, and the ’get’ method to retrieve the result of the computation. From the second interface, the class inherited the `run` method to execute a task (either ’Runnable’ or ’Callable’) in an ’ExecutorService’.

But the the `Future` interface has significant limitations:



*   it’s impossible to register a callback on future competition - the only possible way is to check `isDone` in cycle (to busy-wait)
*   it’s impossible to combining results of two computations both when they’re independent or when the second computation depends on the result of the first
*   it’s impossible to manually complete a future either with a result or with an exception

To change this, in Java 8 was added the `CompletionStage` interface and its base implementation the `CompletableFuture` class. These classes allow to build effective multi-step compurations of a single result, when the compurations steps form either a chain or a three or a directed acyclic graph.

>In Java 9 and Java 12, the `CompletionStage` interface and the `CompletableFuture` class were significantly updated.


## The [CompletionStage](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/util/concurrent/CompletionStage.html) interface

The `CompletionStage` interface represents a step in a multi-step computation that can be executed synchronously or asynchronously. A stage can either compute a value (returns a single result) or performs an action (returns no result). A stage can be initiated by finishing one or two previous stages and can initiate a subsequent stage.

The CompletionStage interface was designed to overcome the limitations of the `Future` interface in implementing chained asynchronous computations. The Future interface has only the blocking `get` method that waits until for the computation to complete and the non-blocking `isDone` method that checks whether this future is completed.


```
// an example of synchronous multi-step computation
int amountInUsd = (getPriceInGbp() * getExchangeRateGbpToUsd()) + (getPriceInEur() * getExchangeRateEurToUsd());
```


Most of the methods of CompletionStage return CompletionStage that allows chaining of stages. Note that during the changing, the methods aren’t executed immediately, but are registered for further executions like callbacks. The methods are executed then the given `CompletionStage` completes.

Methods can be classified in two ways: _how they do_ and _what they do_.


### Naming convention

The `CompletionStage` interface has 43 methods which at first glance may seem confusing, but in fact, it has three distinguished naming patterns.

The _first_ naming pattern explains how a new stage is initiated:



*   if a method’s name has the prefix `then`, then the new stage is initiated after completion of a single previous stage.
*   if a method’s name has the prefix `either`, then the new stage is initiated after completion of the first of two previous stages.
*   if a method’s name has the prefix `both`, then the new stage is initiated after completion of both previous stages.

The _second_ naming pattern explains what computations perform the new stage:



*   if a method’s name has the `apply` fragment, then the new stage applies an argument by a `Function` (takes argument(s) and returns one result).
*   if a method’s name has the `accept` fragment, then the new stage accepts an argument by a `Consumer` (takes argument(s) and returns no result).
*   if a method’s name has the `run` fragment, then the new stage runs an action by a `Runnable` (takes no argument and returns no result).

>If the new stage depends on both previous stages (has two arguments), it uses `BiFunction` instead of `Function` and `BiConsumer` instead of `Consumer`.


<table>
  <tr>
   <td rowspan="2" >prefix
<p>
vs.
<p>
computation type
   </td>
   <td colspan="2" >takes argument(s) and returns a result
   </td>
   <td colspan="2" >takes argument(s) and returns no result
   </td>
   <td>takes no argument and returns no result
   </td>
  </tr>
  <tr>
   <td>`Function`
   </td>
   <td>`BiFunction`
   </td>
   <td>`Consumer`
   </td>
   <td>`BiConsumer`
   </td>
   <td>`Runnable`
   </td>
  </tr>
  <tr>
   <td>`then`
   </td>
   <td>`thenApply`, `thenCompose`
   </td>
   <td>
   </td>
   <td>`thenAccept`
   </td>
   <td>
   </td>
   <td>`thenRun`
   </td>
  </tr>
  <tr>
   <td>`either`
   </td>
   <td>`applyToEither`
   </td>
   <td>
   </td>
   <td>`acceptEither`
   </td>
   <td>
   </td>
   <td>`runAfterEither`
   </td>
  </tr>
  <tr>
   <td>`both`
   </td>
   <td>
   </td>
   <td>`thenCombine`*
   </td>
   <td>
   </td>
   <td>`thenAcceptBoth`
   </td>
   <td>`runAfterBoth`
   </td>
  </tr>
</table>


>Note that the `thenCombine` method could have been named `thenApplyBoth`.

The _third_ naming pattern explains what thread executes the new stage:



*   if a method has a structure like `something(...)` then the new stage is executed by the default facility (that can be synchronous or asynchronous).
*   if a method has a structure like `somethingAsync(...)` then the new stage is executed by the default asynchronous facility.
*   if a method has a structure like `somethingAsync(..., Executor)` then the new stage is executed by the supplied `Executor`.

If stages are independent and so can be executed in parallel, the asynchronous execution can give a significant performance improvement (of course if you have enough processor cores). However, if tasks are short (hundreds of milliseconds), then context switching between threads can introduce significant overhead.

>Note that the _default facility_ and the _default asynchronous facility_ are specified by `CompletionStage` implementations, not by the interface itself. Looking ahead, the `CompletableFuture` implementation of the `CompletionStage` interface uses the thread that completes the stage as the _default facility_ and `ForkJoinPool.commonPool()` as the _default asynchronous facility_.


### Classification of methods

Methods of the `CompletionStage` interface can be divided into two groups by their purpose:



*   methods to perform computations
*   methods to handle exceptions

>The `CompletionStage` interface doesn’t contain methods for stage creation nor stage status checking nor stage completion. This functionality is delegated to `CompletionStage` implementations.


#### Methods to perform computations


##### The `Function`/`BiFunction` methods

The `thenApply` method creates a new stage that upon completion applies the given `Function` to the result of the single previous stage.

The `applyToEither` method creates a new stage that upon completion applies the given `Function` to the result of this stage _or_ another stage (which completes first).

The `thenCombine` method creates a new stage that upon completion applies the given `BiFunction` to the results of this stage _and_ another stage.


##### The thenCompose method

The thenCompose method is similar to the thenApply method - both apply a `Function` as an argument. The difference is in the purpose of these methods. The thenApply method is designed to apply a _fast_ function. The thenCompose method is designed to apply a slow function, and the result of the function is a subtype of CompletionStage.

`thenApply` vs. `thenCompose`

The `thenApply` method should be used when the applied function is a quick synchronous action that returns a value rather than a stage. The `thenCompose` method should be used when the applied function is a long asynchronous action that returns a stage.


```
thenApply with quick function
thenApply with long function
thenCompose with long function
```


If you use the `thenApply` method with a function that returns `CompletionStage&lt;T>`, then the result of the thenApply method will be `CompletionStage&lt;CompletionStage&lt;T>>`. Then to convert the result to `CompletionStage&lt;T>` for further processing you will need to call the blocking `get` method, which is highly discouraged in non-blocking stages processing. To avoid blocking calls, you should use the `thenCompose` method. The method performs conversion from `CompletionStage&lt;CompletionStage&lt;T>>` to `CompletionStage&lt;T>` in a non-blocking way. This is similar to how the `Stream.flatMap` method converts `Stream&lt;Stream&lt;T>>` to just `Stream&lt;T>`.

`thenCompose` vs `thenCombine`

The `thenCompose` method is used to perform two stages _sequentially_ when the second stage accepts as the argument the result of the first stage. The `thenCombine` method is used to perform two stages _parallelly._


```
thenCompose with sequential stages
thenCombine with parallel stages
```



##### The Consumer/BiConsumer methods

The `thenAccept` method creates a new stage that upon completion supplies the result of this stage to the given `Consumer`.

The `acceptEither` method creates a new stage that upon completion supplies the result of this stage _or_ another stage the given `Consumer`(which completes first).

The `thenAcceptBoth` method creates a new stage that upon completion supplies the result of this stage _and_ another stage the given `BiConsumer`.


##### The `Runnable` methods

The `thenRun` method creates a new stage that upon completion runs the given `Runnable` after the execution of this stage.

The `runAfterEither` method creates a new stage that upon completion runs the given `Runnable` after the execution of this stage _or_ other stage.

The `runAfterBoth` method creates a new stage that upon completion runs the given `Runnable` after the execution of this stage _and_ another stage.

Summary of the methods


<table>
  <tr>
   <td>
   </td>
   <td>Result
   </td>
   <td>Method name
   </td>
   <td>Other stage parameter
   </td>
   <td>Computation parameter
   </td>
  </tr>
  <tr>
   <td rowspan="4" >takes argument(s) and returns a result
   </td>
   <td>CompletionStage&lt;U>
   </td>
   <td>thenApply
   </td>
   <td>
   </td>
   <td>Function&lt;? super T,? extends U> fn
   </td>
  </tr>
  <tr>
   <td>CompletionStage&lt;U>
   </td>
   <td>thenCompose
   </td>
   <td>
   </td>
   <td>Function&lt;? super T,? extends CompletionStage&lt;U>> fn
   </td>
  </tr>
  <tr>
   <td>CompletionStage&lt;U>
   </td>
   <td>applyToEither
   </td>
   <td>CompletionStage&lt;? extends T> other 
   </td>
   <td>Function&lt;? super T,U> fn
   </td>
  </tr>
  <tr>
   <td>CompletionStage&lt;V>
   </td>
   <td>thenCombine
   </td>
   <td>CompletionStage&lt;? extends U> other
   </td>
   <td>BiFunction&lt;? super T,? super U,? extends V> fn
   </td>
  </tr>
  <tr>
   <td rowspan="3" >takes argument(s) and returns no result
   </td>
   <td>CompletionStage&lt;Void>
   </td>
   <td>thenAccept
   </td>
   <td>
   </td>
   <td>Consumer&lt;? super T> action
   </td>
  </tr>
  <tr>
   <td>CompletionStage&lt;Void>
   </td>
   <td>acceptEither
   </td>
   <td>CompletionStage&lt;? extends T> other
   </td>
   <td>Consumer&lt;? super T> action
   </td>
  </tr>
  <tr>
   <td>CompletionStage&lt;Void>
   </td>
   <td>thenAcceptBoth
   </td>
   <td>CompletionStage&lt;? extends U> other
   </td>
   <td>BiConsumer&lt;? super T,? super U> action
   </td>
  </tr>
  <tr>
   <td rowspan="3" >takes no argument and returns no result
   </td>
   <td>CompletionStage&lt;Void>
   </td>
   <td>thenRun
   </td>
   <td>
   </td>
   <td>Runnable action
   </td>
  </tr>
  <tr>
   <td>CompletionStage&lt;Void>
   </td>
   <td>runAfterEither
   </td>
   <td>CompletionStage&lt;?> other                                   <a href="https://docs.oracle.com/javase/9/docs/api/java/lang/Runnable.html"> </a>
   </td>
   <td>Runnable action
   </td>
  </tr>
  <tr>
   <td>CompletionStage&lt;Void>
   </td>
   <td>runAfterBoth 
   </td>
   <td>CompletionStage&lt;?> other
   </td>
   <td>Runnable action
   </td>
  </tr>
</table>


Why methods that return `CompletionStage&lt;Void>` can be useful? They can perform computations with side-effects and can signalize the fact of completion of computation either with result or with an exception.


#### The methods to handle exceptions

Any blocking, synchronous computation can finish normally or throw an exception (either unchecked or checked). A possible asynchronous stage can finish normally or throw an exception as well. But because the stages can be executed in different threads, it’s impossible to use a `try-catch` statement in one thread to catch an exception thrown from another thread. To handle exceptions in chained stages there are special methods in the` CompletionStage` interface.

>If a stage is completed exceptionally, then all other stages further in the computation chain will be completed exceptionally as well.

The `whenComplete` method registers a `BiConsumer` which will be called when this stage completes either normally or exceptionally. The consumer accepts the result (or `null` if none) and the exception (or `null` if none). This method can perform some action with either a successfult result or an exception, but it can’t change the result that is propagated to the next stage. This method preserves the result of the triggering stage instead of computing a new one (so an exception is not propagated to the next stage).


```
CompletionStage<String> stage = ...
       .whenComplete((result, throwable) -> {
           if (throwable == null) {
               logger.info("result: {}", result);
           } else {
               logger.error("exception: {}", throwable);
           }
       });
```


The `handle` method registers a `BiFunction` which will be called when this stage completes either normally or exceptionally. The function applies as arguments the result (or `null` if none) and the exception (or `null` if none) and returns some result. This method can convert a successfult result and can replace an exception with some fallback value (so an exception is not propagated to the next stage).


```
CompletionStage<String> stage = ...
       .handle((result, throwable) -> {
           if (throwable == null) {
               return throwable == null ? null : value.toUpperCase();
           } else {
               logger.error("exception: {}", throwable);
               return throwable.getMessage();
           }
       });
```


The `exceptionally` method registers a `Function` which will be called when this stage completes exceptionally. The function applies as arguments the exception and returns some result. This method can replace an exception with some fallback value (so an exception is not propagated to the next stage).


```
CompletionStage<String> stage = ...
       .exceptionally(throwable -> {
               logger.error("exception: {}", throwable);
               return throwable.getMessage();
           }
       });
```


The `exceptionallyCompose` method applies the exception of this stage to the given Function that returns a subtype of CompletionStage. This method is executed whether the stage is completed exceptionally allows to set the result of the stage, when the recovery computation after exception is executed asynchronously

Summary of the methods


<table>
  <tr>
   <td>
   </td>
   <td>
   </td>
   <td>Result
   </td>
   <td>Method name
   </td>
   <td>Parameter
   </td>
  </tr>
  <tr>
   <td>can not to modify result
   </td>
   <td rowspan="2" >called on success or exception
   </td>
   <td>CompletionStage&lt;T>
   </td>
   <td>whenComplete
   </td>
   <td>BiConsumer&lt;? super T,? super Throwable> action
   </td>
  </tr>
  <tr>
   <td rowspan="3" >can modify result
   </td>
   <td>CompletionStage&lt;U>
   </td>
   <td>handle
   </td>
   <td>BiFunction&lt;? super T,Throwable,? extends U> fn
   </td>
  </tr>
  <tr>
   <td rowspan="2" >called on exception
   </td>
   <td>CompletionStage&lt;T>
   </td>
   <td>exceptionally 
   </td>
   <td>Function&lt;Throwable,​? extends T> fn
   </td>
  </tr>
  <tr>
   <td>CompletionStage&lt;T>
   </td>
   <td>exceptionallyCompose 
   </td>
   <td>Function&lt;Throwable,​? extends CompletionStage&lt;T>> fn
   </td>
  </tr>
</table>



### The [CompletableFuture](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/CompletableFuture.html) class

The `CompletableFuture` class implements both the `CompletionStage` and `Future` interfaces. That means the class can represent the result of asycnhrounous computations (the whole) and a step in a multi-step computation (a part).
