# Java parallelism: CompletionStage and CompletableFuture


## Introduction

The `CompletionStage` and `CompletableFuture`classes are high-level parallel Java API that was added in Java 8 ~~to have the ability~~ to join multiple asynchronous computations into a _single_ ~~asynchronous~~ result without the mess of nested callbacks (‘callback hell’).

Before adding these classes Java already had much simpler classes to manage asynchronous computations - the `Future` interface and its base implementation the `FutureTaks` class were added in Java 5.

The `Future` interface represents the _result_ of an asynchronous computation which has a few methods:



*   to check whether the computation is completed or canceled
*   to cancel the computation
*   to wait if necessary for the computation to complete, and then to retrieve its result

Basically, the `Future` interface represents an incompleted result of submitting a task (either ’Runnable’ or ’Callable’) for the asynchronous execution to an ’ExecutorService’ instance that have blocking ’get’ method and non-blocking ’isDone’ method to wait for the completion of the result.

But the the `Future` interface has significant limitations in building multi-step asynchronous computations:



*   it’s impossible to register a callback on future competition - the only possible way is to check the result of the `isDone` method in a cycle (to busy-wait)
*   it’s impossible to combine the results of two computations in a non-blocking way
*   it’s impossible to manually complete a future either with a result or with an exception

To change this, in Java 8 was added the `CompletionStage` interface and its base implementation - the `CompletableFuture` class. These classes allow building effective multi-step computations of a single result - not only in simple linear sequences of computation steps but also when the computation steps form dependencies as complicated as directed acyclic graphs.

>The `CompletionStage` interface and the `CompletableFuture` class were updated in Java 9 and Java 12.


## Futures and promises

The concept of a _future/promise_ exists in many programming languages (JavaScript: `Promise`; Java: `Future`, `CompletionStage`, `CompletableFuture`, Google Guava [ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained); Scala: `scala.concurrent.Future`) that allows writing asynchronous code that still has a _fluent interface_ as synchronous code.

In a broader sense, the terms _future_ and _promise_ are ~~often ~~used interchangeably, as a placeholder for a parallel-running operation that is ongoing (that hasn't completed yet) but is expected in the future.

In a narrower sense, a _future_ is a read-only placeholder view of a result (that is yet unavailable), while a _promise_ is a writable, single-assignment object that sets the value of the _future_. A promise can complete the future with a result to indicate success, or with an exception to indicate failure.

>Importantly, futures/promises typically are asynchronous but do exist synchronous futures/promises: e.g. in JavaScript that is single-threaded and inheritably synchronous.

The following example can help you to understand the idea of future/promise. A consumer sends a task to a producer to be executed asynchronously. The producer creates a promise when it starts that task and begins the required work and sends a future to a consumer. The consumer receives a future that isn’t completed yet and waits for its completion. During waiting, the consumer isn’t blocked and can execute other tasks. When the producer completes the task, it fulfills the promise and thereby provides the future's value. Essentially the promise represents the producing end of the future/promise relationship, while the future represents the consuming end. This explains why promises are single write-only and futures are multiple read-only.

![Future and Promise](/images/future_and_promise.png)

The most important part of futures/promises is the possibility to define a pipeline of operations to be invoked upon completion of the task represented by the future/promise. In comparison with asynchronous callbacks, this allows writing more fluent code that supports the composition of nested result/error handlers without ‘callback hell’. In comparison with blocked code, this allows writing quicker asynchronous pipelines without boilerplate synchronization.

In Java, the `Future` interface represents a _future_: has methods to check if the task is complete, to wait for its completion, and to retrieve the result of the task when it is complete. (The `FutureTask` class has the `void set(V v)` method that it is `protected`). The `CompletableFuture` class can be thought of as a _promise_ since their value can be explicitly set. However, `CompletableFuture` also implements the `Future` interface allowing it to be used as a _future_ as well.

