# Java parallelism: CompletionStage and CompletableFuture


## Introduction

The `CompletionStage` and `CompletableFuture`classes are high-level parallel Java API that was added in Java 8 to pipeline multiple asynchronous tasks into a single result without the mess of nested callbacks (‘callback hell’).

Before adding these classes Java already had much simpler classes to manage asynchronous tasks - in Java 5 were added the `Future` interface and its base implementation the `FutureTaks` class.

The `Future` interface represents the _result_ of an asynchronous task that has a few methods:



*   to check whether the task is completed or canceled
*   to cancel the task
*   to wait if necessary for the task to complete, and then to retrieve its result

Basically, the `Future` interface represents an incompleted result of submitting a task (either ’Runnable’ or ’Callable’) for asynchronous execution to an ’ExecutorService’ instance. The incompleted result has the blocking ’get’ method and the non-blocking ’isDone’ method to wait for the completion of the result.

But the the `Future` interface has significant limitations in building multi-step asynchronous tasks:



*   it’s impossible to register a callback on task competition - the only possible way is to check the result of the `isDone` method in a cycle (to busy-wait)
*   it’s impossible to combine the results of two tasks in a non-blocking way
*   it’s impossible to manually complete a task either with a result or with an exception

To change this, in Java 8 were added (and in Java 9 and Java 12 were updated) the `CompletionStage` interface and its base implementation - the `CompletableFuture` class. These classes allow building effective multi-step tasks of a single result - not only in simple linear sequences of task steps but also when the task steps form dependencies as complicated as directed acyclic graphs.


## Futures and promises

The concept of a _future/promise_ exists in many programming languages (JavaScript: `Promise`; Java: `Future`, `CompletionStage`, `CompletableFuture`, Google Guava [ListenableFuture](https://github.com/google/guava/wiki/ListenableFutureExplained); Scala: `scala.concurrent.Future`) that allows writing asynchronous code that still has a _fluent interface_ as synchronous code.

>A _future/promise_ can be in two states: incompleted and completed, and completion can be performed either with a result to indicate success, or with an exception to indicate failure.

In a broader sense, the terms _future_ and _promise_ are often used interchangeably, as a placeholder for a parallel-running task that is ongoing (that hasn't been completed yet) but is expected in the future.

In a narrower sense, a _future_ is a read-only placeholder view of a result (that is yet unavailable), while a _promise_ is a writable, single-assignment object that sets the value of the _future_.

The following example can help you to understand the idea of future/promise. A consumer sends a long-running task to a producer to be executed asynchronously. The producer creates a promise when it starts that task and sends a future to a consumer. The consumer receives the future that isn’t completed yet and waits for its completion. During waiting, the consumer isn’t blocked and can execute other tasks. When the producer completes the task, it fulfills the promise and thereby provides the future's value. Essentially the promise represents the producing end of the future/promise relationship, while the future represents the consuming end. This explains why promises are single write-only and futures are multiple read-only.

![Future and Promise](/images/future_and_promise.png)

The most important part of futures/promises is the possibility to define a pipeline of operations to be invoked upon completion of the task represented by the future/promise. In comparison with _asynchronous callbacks_, this allows writing more fluent code that supports the composition of nested result/error handlers without ‘callback hell’. In comparison with _blocked code_, this allows implementing quicker asynchronous pipelines without boilerplate synchronization.

In Java, the `Future` interface represents a _future_: it has methods to check if the task is complete, to wait for its completion, and to retrieve the result of the task when it is complete. (The `FutureTask` class has the `void set(V v)` method that it is `protected`). The `CompletableFuture` class can be thought of as a _promise_ since their value can be explicitly set by the `complete` and `completeExceptionally` methods. However, `CompletableFuture` also implements the `Future` interface allowing it to be used as a _future_ as well. 
