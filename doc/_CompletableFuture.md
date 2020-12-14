# Java: CompletableFuture

Java's completable future framework provides an asynchronous & reactive concurrent programming model

Supports dependent actions that trigger upon completion of async operations

Async operations can be forked, chained, & joined

Async operations can run concurrently in thread pools
Either a (common) fork-join pool or various types of preor user-defined thread pools

Java completable futures often need no explicit synchronization or threading when developing concurrent apps!
Alleviates many accidental & inherent complexities of concurrent programming
Java class libraries handle locking needed to protect shared mutable state

## The Pros & Cons of Synchrony

Method calls in typical Java programs are largely synchronous i.e., a callee borrows the thread of its caller until its computation(s) finish
Note “request/response” nature of these calls

Pros of synchronous calls:
• “Intuitive” to program & debug, e.g.
    • Maps onto common two-way method patterns
    • Local caller state retained when callee returns

Cons of synchronous calls:
• May not leverage all parallelism available in multi-core systems
    • Blocking threads incur overhead
        • e.g., synchronization, context switching, data movement, & memory management costs
    • Selecting right # of threads is hard
        A large # of threads may help to improve performance, but can also waste resources
        A small # of threads may conserve resources at the cost of performance
• May need to change common fork-join pool size in a Java parallel stream

Asynchronous operations can alleviate limitations with synchronous operations
• Asynchrony is a means of concurrent programming where the caller does not block while waiting for the called code to finish

Pros of asynchronous operations
• Responsiveness
    • A calling thread needn’t block waiting for the async request to complete
• Elasticity
    • Multiple requests can run scalably & concurrently on multiple cores
        • Elasticity is particularly useful to auto-scale computationsin cloud environments

Cons of asynchronous operations
• Unpredictability
    • Response times may not unpredictable due to non-determinism of async operations
    • Results can occur in a different order than the original calls were made
• Complicated programming & debugging
    • The patterns & best-practices of asynchronous programming are not well understood
    • Errors can be hard to track due to unpredictability

Two things are necessary for the pros of asynchrony to outweigh the cons
• Performance should improve to offset the increased complexity of programming & debugging
• An asynchronous programming model should reflect the key principles of the reactive paradigm

## CompletionStage

### triggered by a single previous stage
thenApply(Async)
thenAccept(Async)
thenRun(Async)

### triggered by both of two previous stages
thenCombine
thenAcceptBoth
runAfterBoth

### ?
thenCompose(Async)

### triggered by either one of two previous stages
applyToEither
acceptEither
runAfterEither

## CompletableFuture

### methods to create

#### incompleted
constructor
runAsync
supplyAsync

#### completed
completedFuture,completedStage
failedFuture,failedStage

### methods to complete
boolean complete(java.lang.Object)
boolean completeExceptionally(java.lang.Throwable)
boolean cancel(boolean)

### methods to verify
isDone
isCompletedExceptionally
isCancelled

### methods to get
java.lang.Object get()
java.lang.Object get(long,TimeUnit)
java.lang.Object join()
java.lang.Object getNow(java.lang.Object)
CompletableFuture orTimeout(long,TimeUnit)

### methods to handle errors
exceptionally(Async)
handle
whenComplete

### methods with timeout

### methods to combine multiple futures into one
allOf 
anyOf